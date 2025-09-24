package com.ex_dock.ex_dock.backend.v1.router.image

import com.ex_dock.ex_dock.MainVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import java.nio.file.Paths

fun Router.initOpenImageRouter(vertx: Vertx) {
  val openImageRouter = Router.router(vertx)

  openImageRouter["/get/:path"].handler { ctx ->
    var path = ctx.pathParam("path")
    path = path.replace("%2F", "/")
    path = "application-files/$path"
    val fullPath = Paths.get(path)

    if (!fullPath.toFile().exists()) {
      ctx.fail(404, Error("Image does not exist"))
      return@handler
    }

    val contentType: String = when (fullPath.toFile().extension) {
      "png" -> "image/png"
      "jpg" -> "image/jpg"
      "jpeg" -> "image/jpeg"
      "webp" -> "image/webp"
      else -> "unknown"
    }

    if (contentType == "unknown") {
      MainVerticle.logger.error { "Incorrect file type" }
      ctx.fail(404, Error("Image does not exist"))
      return@handler
    }

    val image = fullPath.toFile()

    ctx.response().putHeader("Content-Type", contentType)
      .end(Buffer.buffer(image.readBytes()))
}

this.route("/openImage*").subRouter(openImageRouter)
}
