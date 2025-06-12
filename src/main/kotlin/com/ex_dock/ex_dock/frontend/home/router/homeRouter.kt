package com.ex_dock.ex_dock.frontend.home.router

import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.Router

fun Router.initHome(eventBus: EventBus) {
  this.get("/").handler { ctx ->
    eventBus.request<String>("frontend.retrieveHTML.home", "").onFailure { err ->
      ctx.end("You've successfully accessed the '/' path for the exDock server, but the homepage failed to load")
    }.onSuccess { res ->
      ctx.end(res.body())
    }
  }
}
