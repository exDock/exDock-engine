package com.ex_dock.ex_dock.backend

import com.ex_dock.ex_dock.backend.v1.router.enableBackendV1Router
import com.ex_dock.ex_dock.backend.v1.router.websocket.initWebsocket
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler

fun Router.enableBackendRouter(vertx: Vertx) {
  val backendRouter: Router = Router.router(vertx)

  backendRouter.route().handler(CorsHandler.create())

  backendRouter.get("/about").handler { ctx ->
    ctx.end("about page for the APIs")
  }

  backendRouter.enableBackendV1Router(vertx)
  backendRouter.initWebsocket(vertx)

  this.route("$apiMountingPath*").subRouter(backendRouter)
}
