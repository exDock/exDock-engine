package com.ex_dock.ex_dock.backend.v1.router.auth

import com.ex_dock.ex_dock.backend.apiMountingPath
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

  authRouter.route().handler(BodyHandler.create())

  authRouter.post("/token").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val usernamePasswordCredentials = UsernamePasswordCredentials(
      body.getString("email"),
      body.getString("password")
    )
    eventBus.request<String>(
      "process.authentication.login", usernamePasswordCredentials, usernamePasswordCredentialsDeliveryOptions)
      .onFailure { exception ->
      if (exception.message == "invalid credentials") {
        ctx.fail(403, Error("invalid credentials"))
      } else {
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
    val refreshToken = body.getString("refresh_token")
    eventBus.request<String>("process.authentication.refresh", refreshToken)
      .onFailure { exception ->
        if (exception.message == "invalid refresh token") {
          ctx.fail(403, Error("invalid refresh token"))
        } else {
          ctx.fail(500, Error("internal server error"))
        }
      }.onSuccess { message ->
        ctx.response().putHeader("content-type", "application/json")
          .end(JsonObject()
            .put("access_token", message.body())
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
