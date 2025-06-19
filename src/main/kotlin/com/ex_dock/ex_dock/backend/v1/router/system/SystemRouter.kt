package com.ex_dock.ex_dock.backend.v1.router.system

import com.ex_dock.ex_dock.backend.v1.router.docker.ServerHealth
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.ext.web.Router

fun Router.enableSystemRouter(vertx: Vertx) {
  val systemRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  systemRouter["/getSettings"].handler { ctx ->
    eventBus.request<String>("process.system.getVariables", "").onFailure { error ->
      ctx.response().setStatusCode(400).end(error.message)
    }.onSuccess { message ->
      ctx.response().end(message.body())
    }
  }

  systemRouter.post("/setSettings").handler { ctx ->
    eventBus.request<String>("process.system.saveVariables", ctx.body().asJsonObject()).onFailure { error ->
      ctx.response().setStatusCode(400).end(error.message)
    }.onSuccess {
      eventBus.request<Unit>("process.docker.serverHealth", ServerHealth.RESTARTING, DeliveryOptions().setCodecName("ServerHealthCodec"))

      ctx.response().setStatusCode(200).end("Successfully saved settings")
    }
  }


  this.route("/system*").subRouter(systemRouter)
}
