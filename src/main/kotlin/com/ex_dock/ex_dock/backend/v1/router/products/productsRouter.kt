package com.ex_dock.ex_dock.backend.v1.router.products

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

fun Router.initProductsRouter(vertx: Vertx) {
  val productsRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
}
