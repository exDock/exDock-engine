package com.ex_dock.ex_dock.backend.v1.router.url

import com.ex_dock.ex_dock.MainVerticle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.initUrlRouter(vertx: Vertx) {
  val urlRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  urlRouter["/getAll"].handler { ctx ->
    eventBus.request<List<JsonObject>>("process.url.getAllUrlKeys", "").onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(JsonObject().put("urls", it.body()).encode())
    }
  }

  urlRouter["/get/:key"].handler { ctx ->
    val key = ctx.pathParam("key")
    eventBus.request<JsonObject>("process.url.getUrlByKey", key).onFailure {
      ctx.fail(500, it)
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().encode())
    }
  }

  urlRouter.post("/create").handler { ctx ->
    val body = ctx.body().asJsonObject()
    eventBus.request<JsonObject>("process.url.createUrlKey", body).onFailure {
      ctx.fail(500, it)
      MainVerticle.logger.error { it.localizedMessage }
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().encode())
    }
  }

  urlRouter.put("/update").handler { ctx ->
    val body = ctx.body().asJsonObject()
    eventBus.request<JsonObject>("process.url.updateUrlKey", body).onFailure {
      ctx.fail(500, it)
      MainVerticle.logger.error { it.localizedMessage }
    }.onSuccess {
      ctx.response().putHeader("Content-Type", "application/json")
        .end(it.body().encode())
    }
  }

  urlRouter.delete("/delete/:key").handler { ctx ->
    val key = ctx.pathParam("key")
    eventBus.request<String>("process.url.deleteUrlKey", key).onFailure {
      ctx.fail(500, it)
      MainVerticle.logger.error { it.localizedMessage }
    }.onSuccess {
      ctx.end(it.body())
    }
  }


  this.route("/url*").subRouter(urlRouter)
}
