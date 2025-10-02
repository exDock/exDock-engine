package com.ex_dock.ex_dock.backend.v1.router.template

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.template.Template
import com.ex_dock.ex_dock.database.template.toDocument
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.initTemplateRouter(vertx: Vertx) {
  val templateRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
  val templateDeliveryOptions = DeliveryOptions().setCodecName("TemplateCodec")

  templateRouter["/getAll"].handler { ctx ->
    eventBus.request<List<JsonObject>>("process.template.getAllTemplates", "").onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(JsonObject().put("templates", it.body()).encode())
    }
  }

  templateRouter["/get/:key"].handler { ctx ->
    val key = ctx.pathParam("key")
    eventBus.request<JsonObject>("process.template.getTemplateByKey", key).onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().encode())
    }
  }

  templateRouter.post("/create").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val template = Template.fromJson(body)
    eventBus.request<Template>("process.template.createTemplate", template, templateDeliveryOptions).onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().toDocument().encode())
    }
  }

  templateRouter.put("/update").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val template = Template.fromJson(body)
    eventBus.request<Template>("process.template.updateTemplate", template, templateDeliveryOptions).onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().toDocument().encode())
    }
  }

  templateRouter.delete("/delete/:key").handler { ctx ->
    val key = ctx.pathParam("key")
    eventBus.request<String>("process.template.deleteTemplate", key).onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body())
    }
  }

  templateRouter.post("/generate").handler { ctx ->
    val body = ctx.body().asJsonObject()

    eventBus.request<String>("template.generate.singleUse", body).onFailure {
      MainVerticle.logger.error { it.localizedMessage }
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "text/html")
        .end(it.body())
    }
  }

  this.route("/template*").subRouter(templateRouter)
}
