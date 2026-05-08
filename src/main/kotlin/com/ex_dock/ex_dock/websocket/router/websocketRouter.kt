package com.ex_dock.ex_dock.websocket.router

import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.websocket.page.enableWebsocketPageRouter
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.ext.web.Router

fun Router.enableWebsocketRouter(vertx: Vertx, logger: KLogger) {
  val websocketRouter = Router.router(vertx)

  websocketRouter.enableWebsocketPageRouter(vertx, logger)

  websocketRouter["/about"].handler { ctx ->
    ctx.end("about page for the Websocket")
  }

  this.route().subRouter(websocketRouter)
}
