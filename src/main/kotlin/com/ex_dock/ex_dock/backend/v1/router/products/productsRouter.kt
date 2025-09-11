package com.ex_dock.ex_dock.backend.v1.router.products

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.initProductsRouter(vertx: Vertx) {
  val productsRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  productsRouter.get("/overview").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.product.getAllProducts", "").onFailure { err ->
      ctx.response().setStatusCode(500).end(err.message)
    }.onSuccess { message ->
      ctx.response().setStatusCode(500).end(
          JsonArray(
            message.body()
          ).encode()
        )
    }
  }

  this.route("/products*").subRouter(productsRouter)
}
