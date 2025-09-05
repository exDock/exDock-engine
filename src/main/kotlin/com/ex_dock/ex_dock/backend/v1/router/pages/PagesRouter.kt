package com.ex_dock.ex_dock.backend.v1.router.pages

import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateData
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
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

  pagesRouter["/getPreview"].handler { ctx ->
    val body = ctx.body().asJsonObject()
    val template = body.getString("template")
    val data = body.getJsonObject("data")
    val dataMap = mutableMapOf<String, Any>()

    data.forEach { (key, value) ->
      dataMap[key] = value
    }


    eventBus.request<String>("template.generate.singleUse",
      SingleUseTemplateData(template, dataMap),
      DeliveryOptions().setCodecName("SingleUseTemplateDataCodec")
    ).onFailure {
      ctx.response().setStatusCode(400).end(it.message)
    }.onSuccess {
      ctx.response().end(it.body())
    }
  }

  this.route("/pages*").subRouter(pagesRouter)
}
