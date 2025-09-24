package com.ex_dock.ex_dock.backend.v1.router.products

import com.ex_dock.ex_dock.helper.ctx.errorResponse
import com.ex_dock.ex_dock.helper.ctx.jsonResponse
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.singleProduct(eventBus: EventBus) {
  this.get("/:id").handler { ctx ->
    eventBus.request<JsonObject>("process.product.getProductById", ctx.pathParam("id")).onFailure { err ->
      ctx.errorResponse(err)
    }.onSuccess { res ->
      ctx.jsonResponse(res)
    }
  }
}
