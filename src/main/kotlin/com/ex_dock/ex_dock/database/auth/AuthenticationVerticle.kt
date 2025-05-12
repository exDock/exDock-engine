package com.ex_dock.ex_dock.database.auth

import com.ex_dock.ex_dock.database.account.BackendPermissions
import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.convertUser
import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*

class AuthenticationVerticle: AbstractVerticle() {
  private lateinit var eventBus: EventBus
  private lateinit var jwtAuth: JWTAuth
  private lateinit var authHandler: ExDockAuthHandler

  override fun start() {
    eventBus = vertx.eventBus()
    authHandler = ExDockAuthHandler(vertx)

    setupJwtAuth()

    handleLogin()
    handleRefresh()
    authenticateToken()
  }

  private fun setupJwtAuth() {
    eventBus.consumer<Pair<String, String>>("process.authentication.registerKeys").handler { message ->
      val privateKey = message.body().first
      val publicKey = message.body().second

      val config: JWTAuthOptions = JWTAuthOptions()
        .addPubSecKey(PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setBuffer(privateKey))
        .addPubSecKey(PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setBuffer(publicKey)
        )

      jwtAuth = JWTAuth.create(vertx, config)
      message.reply("")
    }
  }

  private fun handleLogin() {
    eventBus.consumer<UsernamePasswordCredentials>("process.authentication.login").handler { message ->
      val usernamePasswordCredentials = message.body()

      authHandler.authenticate(usernamePasswordCredentials) { result ->
        if (result.succeeded()) {
          val user = result.result()
          val accessTokenOptions = JWTOptions()
            .setAlgorithm("RS256")
            .setExpiresInMinutes(15)
            .setSubject(user.principal().getString("id"))
            .setIssuer("exDock")
          val claims = JsonObject().put("authorizations", user.principal().getJsonArray("authorizations"))
          val accessToken = jwtAuth.generateToken(claims, accessTokenOptions)

          val refreshTokenOptions = JWTOptions()
            .setAlgorithm("RS256")
            .setExpiresInMinutes(60 * 24 * 7)
            .setSubject(user.principal().getString("id"))
            .setIssuer("exDock")
          val refreshClaims = JsonObject()
          refreshClaims.put("jti", "jti-" + UUID.randomUUID().toString())
          val refreshToken = jwtAuth.generateToken(refreshClaims, refreshTokenOptions)

          message.reply(JsonObject()
            .put("access_token", accessToken)
            .put("refresh_token", refreshToken)
            .encode())
        } else {
          message.fail(401, "invalid credentials")
        }
      }
    }
  }

  private fun handleRefresh() {
    eventBus.consumer<String>("process.authentication.refresh").handler { message ->
      val refreshToken = message.body()

      jwtAuth.authenticate(TokenCredentials().setToken(refreshToken)) { result ->
        if (result.succeeded()) {
          val user = result.result()
          val userId = user.principal().getString("sub")
          eventBus.request<FullUser>("process.account.getFullUserByUserId", userId.toInt()).onComplete { userResult ->
            if (userResult.succeeded()) {
              val fullUser = userResult.result().body()
              val convertedUser = fullUser.convertUser(authHandler)
              val permissions = convertedUser.principal().getJsonArray("authorizations")

              val newTokenOptions = JWTOptions()
                .setAlgorithm("RS256")
                .setExpiresInMinutes(15)
                .setSubject(convertedUser.principal().getString("sub"))
                .setIssuer("exDock")
              val newClaims = JsonObject()
                .put("permissions", permissions)
              val newToken = jwtAuth.generateToken(newClaims, newTokenOptions)
              message.reply(newToken)
            } else {
              message.fail(500, "internal server error")
            }
          }
        } else {
          message.fail(401, "invalid refresh token")
        }
      }
    }
  }

  private fun authenticateToken() {
    eventBus.consumer<String>("process.authentication.authenticateToken").handler { message ->
      val token = message.body()

      jwtAuth.authenticate(TokenCredentials().setToken(token)) { result ->
        if (result.succeeded()) {
          message.reply(result.result().principal().getString("userId"))
        } else {
          message.fail(401, "invalid token")
        }
      }
    }
  }
}
