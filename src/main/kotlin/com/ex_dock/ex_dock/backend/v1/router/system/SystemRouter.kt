package com.ex_dock.ex_dock.backend.v1.router.system

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

fun Router.enableSystemRouter(vertx: Vertx) {
  val systemRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  systemRouter["/test"].handler { ctx ->
    eventBus.request<String>("process.system.getVariables", "").onFailure { error ->
      ctx.response().setStatusCode(400).end(error.message)
    }.onSuccess { message ->
      ctx.response().end(message.body())
    }
  }


  this.route("/system*").subRouter(systemRouter)
}
