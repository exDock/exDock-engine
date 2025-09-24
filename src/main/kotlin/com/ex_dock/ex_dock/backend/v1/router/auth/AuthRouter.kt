package com.ex_dock.ex_dock.backend.v1.router.auth

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

fun Router.enableAuthRouter(vertx: Vertx, absoluteMounting: Boolean = false) {
  val usernamePasswordCredentialsDeliveryOptions = DeliveryOptions().setCodecName("UsernamePasswordCredentialsCodec")
  val authRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
    .registerGenericCodec(UsernamePasswordCredentials::class)

  authRouter.route().handler(BodyHandler.create())

  authRouter.post("/token").handler { ctx ->
    val body = ctx.body().asJsonObject()
    if (body == null || body.getString("email") == null || body.getString("password") == null) {
      ctx.fail(400, Error("email and password are required"))
      return@handler
    }
    val usernamePasswordCredentials = UsernamePasswordCredentials(
      body.getString("email"),
      body.getString("password")
    )
    eventBus.request<String>(
      "process.authentication.login", usernamePasswordCredentials, usernamePasswordCredentialsDeliveryOptions)
      .onFailure { exception ->
      if (exception.message == "invalid credentials") {
        ctx.fail(401, Error("invalid credentials"))
      } else {
        MainVerticle.logger.error { exception.message }
        ctx.fail(500, Error("internal server error"))
      }
    }.onSuccess { message ->
        ctx.response().putHeader("Content-Type", "application/json")
          .end(JsonObject()
            .put("tokens", message.body())
            .encode()
          )
      }
  }

  authRouter.post("/refresh").handler { ctx ->
    val body = ctx.body().asJsonObject()
    if (body == null || body.getString("refresh_token") == null) {
      ctx.fail(400, Error("refresh token is required"))
      return@handler
    }
    val refreshToken = body.getString("refresh_token")
    eventBus.request<JsonObject>("process.authentication.refresh", refreshToken)
      .onFailure { exception ->
        if (exception.message == "invalid refresh token") {
          ctx.fail(401, Error("invalid refresh token"))
        } else {
          ctx.fail(500, Error("internal server error"))
        }
      }.onSuccess { message ->
        val response = message.body()
        ctx.response().putHeader("content-type", "application/json")
          .end(JsonObject()
            .put("access_token", response.getString("access_token"))
            .put("refresh_token", response.getString("refresh_token"))
            .encode()
          )
      }
  }

  authRouter["/ping"].handler { ctx ->
    ctx.response().end("Server responded!")
  }

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1*" else "/v1*"
  ).subRouter(authRouter)
}
