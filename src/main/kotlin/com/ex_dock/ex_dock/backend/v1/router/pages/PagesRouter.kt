package com.ex_dock.ex_dock.backend.v1.router.pages

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.enablePagesRouter(vertx: Vertx) {
  val pagesRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  pagesRouter["/getPage/:key"].handler { ctx ->
    val key = ctx.pathParam("key").replace("%2F", "/")

    eventBus.request<JsonObject>("process.template.getTemplateByKey", key).onFailure {
      ctx.response().setStatusCode(400).end(it.message)
    }.onSuccess {
      val template = it.body()
      ctx.response().end(template.encodePrettily())
    }
  }

  this.route("/pages*").subRouter(pagesRouter)
}
