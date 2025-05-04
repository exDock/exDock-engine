package com.ex_dock.ex_dock.database.auth

import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.time.Instant
import java.util.*

class AuthenticationVerticle: AbstractVerticle() {
  private lateinit var eventBus: EventBus
  private lateinit var jwtAuth: JWTAuth
  private val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
  private val keyPair: KeyPair = generator.generateKeyPair()
  private val authHandler = ExDockAuthHandler(vertx)

  private companion object {
    const val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
    const val END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----"
    const val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
    const val END_PUBLIC_KEY = "\n-----END PUBLIC KEY-----"
  }

  override fun start() {
    setupJwtAuth()

    eventBus = vertx.eventBus()
  }

  private fun setupJwtAuth() {
    val privateKey = BEGIN_PRIVATE_KEY +
      Base64.getMimeEncoder().encodeToString(keyPair.private.encoded) +
      END_PRIVATE_KEY
    val publicKey = BEGIN_PUBLIC_KEY +
      Base64.getMimeEncoder().encodeToString(keyPair.public.encoded) +
      END_PUBLIC_KEY

    val config: JWTAuthOptions = JWTAuthOptions()
      .addPubSecKey(PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer(privateKey))
      .addPubSecKey(PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer(publicKey)
      )

    jwtAuth = JWTAuth.create(vertx, config)
  }

  private fun handleLogin() {
    eventBus.consumer<UsernamePasswordCredentials>("process.authentication.login").handler { message ->
      val credentials = message.body()
      authHandler.authenticate(credentials)
    }
  }

  private fun generateAccessToken(userId: String, permissions: JsonArray): String {
    val now: Instant = Instant.now()

    val claims: JsonObject = JsonObject()
      .put("sub", userId)
      .put("iat", now.epochSecond)
      .put("exp", now.plusSeconds(900).epochSecond)
      .put("permissions", permissions)

    return jwtAuth.generateToken(claims, JWTOptions().setAlgorithm("RS256"))
  }

  private fun generateRefreshToken(userId: String): String {
    val now: Instant = Instant.now()

    val claims: JsonObject = JsonObject()
      .put("sub", userId)
      .put("iat", now.epochSecond)
      .put("exp", now.plusSeconds(604800).epochSecond)
      .put("type", "refresh")

    return jwtAuth.generateToken(claims, JWTOptions().setAlgorithm("RS256"))
  }
}
