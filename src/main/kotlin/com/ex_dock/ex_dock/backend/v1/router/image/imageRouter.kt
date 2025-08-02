package com.ex_dock.ex_dock.backend.v1.router.image

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

fun Router.initImage(vertx: Vertx) {
  val imageRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  imageRouter.post().handler(BodyHandler.create()
    .setUploadsDirectory("src/main/resources/images")
    .setMergeFormAttributes(true))

  imageRouter.post("/").handler(StaticHandler.create("src/main/resources/images").setCachingEnabled(false))

  imageRouter.post("/upload/:path").handler { ctx ->
    val path = ctx.request().getParam("path")
    val body = ctx.body().asString()
    eventBus.send("process.service.convertImage", JsonObject()
      .put("path", path)
      .put("body", body))
    ctx.end("request to imageRouter successful")
  }

  this.route("/image*").subRouter(imageRouter)
}
