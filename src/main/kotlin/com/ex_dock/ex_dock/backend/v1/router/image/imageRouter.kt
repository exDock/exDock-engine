package com.ex_dock.ex_dock.backend.v1.router.image

import com.ex_dock.ex_dock.MainVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.extension

fun Router.initImage(vertx: Vertx) {
  val imageRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  imageRouter.post().handler(BodyHandler.create()
    .setUploadsDirectory("application-files/images")
    .setMergeFormAttributes(true))

  imageRouter.post("/").handler(StaticHandler.create("application-files/images").setCachingEnabled(false))

  imageRouter.post("/:path").handler { ctx ->
    var path = ctx.pathParam("path")
    path = path.replace("%2F", "/")
    eventBus.send("process.service.convertImage", path)
    ctx.end("request to imageRouter successful")
  }

  imageRouter.get("/get/:path").handler { ctx ->
    var path = ctx.pathParam("path")
    path = path.replace("%2F", "/")
    path = "application-files/$path"
    val imagePath = Paths.get(path)

    // Check if the requested image exists
    if (!Files.exists(imagePath)) {
      MainVerticle.logger.error { "Image not found: $path" }
      ctx.response().setStatusCode(404).end("Image not found")
    }

    try {
      // Read the image file into a byte array
      val imageData: ByteArray = Files.readAllBytes(imagePath)

      // Get the image extension type
      var extension = imagePath.extension
      if (extension == "jpg") extension = "jpeg"
      val contentType = "image/$extension"

      ctx.response().putHeader("Content-Type", contentType)
      ctx.response().end(Buffer.buffer(imageData))
    } catch (e: IOException) {
      MainVerticle.logger.error { "Error reading image file: $path" }
      ctx.response().setStatusCode(500).end("Error reading image file")
    }

  }

  this.route("/image*").subRouter(imageRouter)
}
