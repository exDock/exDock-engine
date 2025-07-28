package com.ex_dock.ex_dock.backend.v1.router.file

import com.ex_dock.ex_dock.database.backend_block.FullBlockInfo
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import kotlinx.serialization.Serializable
import java.nio.file.Paths
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.isDirectory

@OptIn(ExperimentalEncodingApi::class)
fun Router.initFileRouter(vertx: Vertx) {
  val fileRouter = Router.router(vertx)

  fileRouter["/getAll"].handler { ctx ->
    val fullList = getRootFiles()

    ctx.response().putHeader("Content-Type", "application/json")
      .end(JsonObject().put("files", fullList).encode())
  }

  fileRouter["/getAll/:path"].handler { ctx ->
    val folders = emptyList<EngineFile>().toMutableList()
    val files = emptyList<EngineFile>().toMutableList()
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
          folders.add(engineFile)
        } else {
          val engineFile = EngineFile(file.name, file.extension, file.length().toInt())
          files.add(engineFile)
        }
      }

      val fullList = folders + files
      ctx.response().putHeader("Content-Type", "application/json")
        .end(JsonObject().put("files", fullList).encode())
    } else {
      val contentType: String = when (fullPath.toFile().extension) {
        "png" -> "image"
        "jpg" -> "image"
        "jpeg" -> "image"
        "gif" -> "image"
        "avif" -> "image"
        "md" -> "markdown"
        "mp4" -> "mp4"
        "pdf" -> "pdf"
        "txt" -> "plain"
        "webp" -> "image"
        else -> "unknown"
      }

      ctx.response().putHeader("Content-Type", "application/octet-stream")
        .end(
          JsonObject()
            .put("contentType", contentType)
            .put("fileName", fullPath.toFile().name)
            .put("fileSize", fullPath.toFile().length())
            .put("fileExtension", fullPath.toFile().extension)
            .put("data", Base64.Default.encode(fullPath.toFile().readBytes())).encode()
        )
    }
  }

  fileRouter["/getBlockData/:blockName"].handler { ctx ->
    val blockName = ctx.pathParam("blockName")

    vertx.eventBus().request<MutableList<FullBlockInfo>>(
      "process.backend_block.getAllFullInfoByBlockNames",
      blockName, DeliveryOptions().setCodecName("ListCodec")
    ).onFailure {
      ctx.fail(500, it)
    }.onSuccess { result ->
      val blocks = result.body()
      val jsonResponse = JsonObject()
      blocks.forEach { block ->
        val blockInformationJson = JsonObject()
        val blockAttributesList = mutableListOf<JsonObject>()
        val fullList = getRootFiles()

        block.blockAttributes.forEach { blockAttribute ->
          if (blockAttribute.attributeName == "Files") {
            val attributeJson = JsonObject()
            attributeJson.put("attribute_id", blockAttribute.attributeId)
            attributeJson.put("attribute_name", blockAttribute.attributeName)
            attributeJson.put("attribute_type", blockAttribute.attributeType)
            attributeJson.put(
              "current_attribute_value",
              fullList
            )
            blockAttributesList.add(attributeJson)
          }
        }

        blockInformationJson.put("block_type", block.backendBlock.blockType)
        blockInformationJson.put("attributes", blockAttributesList)
        jsonResponse.put(block.backendBlock.blockName, blockInformationJson)
      }

      ctx.end(jsonResponse.encode())
    }
  }

  this.route("/file*").subRouter(fileRouter)
}

fun getRootFiles(): List<EngineFile> {
  val folders = emptyList<EngineFile>().toMutableList()
  val files = emptyList<EngineFile>().toMutableList()
  val path = "application-files"
  val fullPath = Paths.get(path)

  fullPath.toFile().listFiles()?.forEach { file ->
    if (file.isDirectory) {
      val engineFile = EngineFile(file.name, "folder", file.length().toInt())
      folders.add(engineFile)
    } else {
      val engineFile = EngineFile(file.name, file.extension, file.length().toInt())
      files.add(engineFile)
    }
  }

  val fullList = folders + files
  return fullList
}

@Serializable
data class EngineFile(
  val fileName: String,
  val extension: String,
  val fileSize: Int
)
