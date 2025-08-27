package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.product.ProductInfo
import io.vertx.core.Future
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Properties

fun EventBus.sendError(error: Exception, targetType: String = "BROADCAST", targetIdentifier: String = "") {
  val errorObject = JsonObject()
    .put("errorType", error::class.simpleName)
    .put("errorMessage", error.message)
    .put("timeStamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))

  this.send("process.websocket.broadcastError", JsonObject()
    .put("errorPayload", errorObject)
    .put("targetType", targetType)
    .put("targetIdentifier", targetIdentifier)
  )
}

fun EventBus.registerVerticleIds(verticleIds: List<String>) {
  this.send("process.main.registerVerticleId", verticleIds, DeliveryOptions().setCodecName("ListCodec"))
}

fun EventBus.convertImage(path: String, imageBytes: String): Future<Unit> {
  return Future.future { future ->
    // Set all the path locations
    val validExtensions = mutableListOf("png", "jpeg", "jpg", "webp")
    val imagePath = System.getProperty("user.dir") + "\\application-files"
    val pathSplit = path.split("/")
    val directorySplit = pathSplit[0].split("\\")
    var directory = imagePath
    val mutableDirectorySplit = directorySplit.toMutableList()
    mutableDirectorySplit.removeAt(mutableDirectorySplit.size - 1)
    mutableDirectorySplit.forEach { part ->
      directory += part + "\\"
    }
    val properties = Properties().load()

    val extension = pathSplit.last().split(".")[1].lowercase()
    val fileName: String = pathSplit.last().split(".")[0]
    var productId = ""
    for (pathItem in pathSplit) {
      if (pathItem == pathSplit[pathSplit.size - 2]) {
        productId = pathItem.split("-")[0]
      }
      if (pathItem != pathSplit.last()) {
        directory += "\\$pathItem"
      }
    }
    val directoryPath = Paths.get(directory)

    // Check if the directory already exists, otherwise make the directory
    if (!Files.exists(directoryPath)) {
      File(directory).mkdirs()
    }

    // Upload new file to the folder
    val newFilePath = directory + "\\" + pathSplit.last()
    val decodedBytes = Base64.getDecoder().decode(imageBytes)
    val newFile = File(newFilePath)
    println(newFile.createNewFile())
    newFile.writeBytes(decodedBytes)
    if (extension == "jpeg") validExtensions.remove("jpg") else validExtensions.remove("jpeg")

    // Get the new uploaded image
    // No need to throw an error here since the original file still is uploaded, but just not converted
    convertToWebp("$directory\\$fileName", extension, newFile).onFailure {
      MainVerticle.logger.error { it.localizedMessage }
    }
    convertToBasicExtensions("$directory\\$fileName", extension, validExtensions, newFile)

    // Update the product to include the new images
    directory = path.replace("/", "%2F").replace(pathSplit.last(), "")
    this.request<JsonObject>("process.product.getProductById", productId).onFailure { err ->
      MainVerticle.logger.error { err.localizedMessage }
      future.fail(err)
    }.onSuccess { res ->
      val product = ProductInfo.fromJson(res.body())
      val newImages = product.images.toMutableList()
      val extensionStrings = emptyList<String>().toMutableList()
      validExtensions.forEach { ext ->
        extensionStrings.add("\"$ext\"")
      }

      newImages.add(
        Image.fromJson(
          JsonObject()
            .put("image_url", "${properties.getProperty("BASE_URL")}:${properties.getProperty("FRONTEND_PORT")}/api/openImage/get/$directory%2F$fileName.${validExtensions.first()}")
            .put("image_name", "$fileName.jpg")
            .put("image_extensions", extensionStrings)
        )
      )
      product.images = newImages

      this.request<JsonObject>("process.product.updateProduct", product, DeliveryOptions().setCodecName("ProductInfoCodec")).onFailure { err ->
        MainVerticle.logger.error { err.localizedMessage }
        future.fail(err)
      }.onSuccess { _ ->
        MainVerticle.logger.info { "Image conversion completed" }
        future.complete()
      }
    }
  }
}
