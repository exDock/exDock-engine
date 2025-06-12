package com.ex_dock.ex_dock.frontend.product.router

import com.ex_dock.ex_dock.helper.sendError
import io.vertx.ext.web.Router
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import java.sql.SQLException

fun Router.initProduct(vertx: Vertx) {
  val productRouter = Router.router(vertx)
  val eventBus: EventBus = vertx.eventBus()

  productRouter.post("/").handler(BodyHandler.create())

  productRouter.get("/").handler { ctx ->
    eventBus.request<JsonObject>("process.products.getAll", "")
      .onSuccess{ reply ->
        ctx.end(reply.body().toString())
      }.onFailure { error ->
        ctx.end("Error retrieving products: ${error.localizedMessage}")
      }
  }

  productRouter.get("/id/:id").handler { ctx ->
    val productId = Json.obj {
      "productId" to ctx.pathParam("id")
    }
    eventBus.request<JsonObject>("process.products.getProductById", productId)
     .onSuccess{ reply ->
        ctx.end(reply.body().toString())
      }.onFailure { error ->
        ctx.end("Error retrieving product with id $productId: ${error.localizedMessage}")
      }
  }

  productRouter.post("/").handler { ctx ->
    val requestBody = ctx.body().asJsonObject()
    eventBus.request<JsonObject>("process.products.createProduct", requestBody)
     .onSuccess{ reply ->
        ctx.end(reply.body().toString())
      }.onFailure { error ->
        ctx.end("Error creating product: ${error.localizedMessage}")
        eventBus.sendError(
          SQLException("Error creating product: ${error.localizedMessage}"),
          "Connection_id",
          ctx.user().principal().getString("userId")
        )
      }
  }

  this.route("/product*").subRouter(productRouter)
}
