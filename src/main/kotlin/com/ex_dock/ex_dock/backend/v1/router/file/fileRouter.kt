package com.ex_dock.ex_dock.backend.v1.router.file

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Paths

fun Router.initFileRouter(vertx: Vertx) {
  val fileRouter = Router.router(vertx)

  fileRouter["/getAll"].handler { ctx ->
    val folders = emptyList<String>().toMutableList()
    val files = emptyList<String>().toMutableList()
    val path = "application-files"
    val fullPath = Paths.get(path)

    fullPath.toFile().listFiles()?.forEach { file ->
      if (file.isDirectory) {
        folders.add(file.name)
        } else {
        files.add(file.name)
      }
    }

    val fullList = folders + files
    ctx.response().putHeader("Content-Type", "application/json")
      .end(JsonObject().put("files", fullList).encode())
  }

  fileRouter["/getAll/:path"].handler { ctx ->
    val folders = emptyList<String>().toMutableList()
    val files = emptyList<String>().toMutableList()
    var path = ctx.pathParam("path")
    path = path.replace("%2F", "/")
    path = "application-files/$path"
    val fullPath = Paths.get(path)

    if (!fullPath.toFile().exists()) {
      ctx.fail(400, Error("Path does not exist"))
    }

    if (fullPath.toFile().isDirectory) {
      fullPath.toFile().listFiles()?.forEach { file ->
        if (file.isDirectory) {
          val engineFile = EngineFile(file.name, "folder", file.length().toInt())
          folders.add(Json.encodeToString(EngineFile.serializer(), engineFile))
        } else {
          val engineFile = EngineFile(file.name, file.extension, file.length().toInt())
          files.add(Json.encodeToString(EngineFile.serializer(), engineFile))
        }
      }

      val fullList = folders + files
      ctx.response().putHeader("Content-Type", "application/json")
        .end(JsonObject().put("files", fullList).encode())
    } else {
      val contentType: String = when (fullPath.toFile().extension) {
        "png" -> "image/png"
        "jpg" -> "image/jpeg"
        "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "avif" -> "image/avif"
        "md" -> "text/markdown"
        "mp4" -> "video/mp4"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
      }

      ctx.response().putHeader("Content-Type", contentType)
        .end(Buffer.buffer(fullPath.toFile().readBytes()))
    }
  }

  this.route("/file*").subRouter(fileRouter)
}

@Serializable
data class EngineFile(
  val fileName: String,
  val extension: String,
  val fileSize: Int
)
