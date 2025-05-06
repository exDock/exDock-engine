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
        ctx.fail(401, Error("invalid credentials"))
      } else {
        ctx.fail(500, Error("internal server error"))
      }
    }.onSuccess { message ->
        ctx.response().putHeader("content-type", "application/json")
          .end(JsonObject()
            .put("tokens", message.body())
            .encode()
          )
      }
  }

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1*" else "/v1*"
  ).subRouter(authRouter)
}
