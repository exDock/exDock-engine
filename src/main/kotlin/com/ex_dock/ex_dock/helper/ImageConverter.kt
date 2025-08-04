package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.luciad.imageio.webp.WebPWriteParam
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import java.util.Properties
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream

fun convertImage(path: String, imageBytes: String, eventBus: EventBus) {
  // Set all the path locations
  val validExtensions = listOf("png", "jpeg", "jpg", "webp").toMutableList()
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
  when (extension) {
      "jpg" -> {
        validExtensions.remove("jpeg")
      }
      "jpeg" -> {
        validExtensions.remove("jpg")
      }
      else -> {
        validExtensions.remove("jpeg")
      }
  }

  // Get the new uploaded image
  convertToWebp("$directory\\$fileName", extension, newFile)
  convertToBasicExtensions("$directory\\$fileName", extension, validExtensions, newFile)

  // Update the product to include the new images
  directory = path.replace("/", "%2F").replace(pathSplit.last(), "")
  eventBus.request<JsonObject>("process.product.getProductById", productId).onFailure { err ->
    MainVerticle.logger.error { err.localizedMessage }
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

    eventBus.request<JsonObject>("process.product.updateProduct", product, DeliveryOptions().setCodecName("ProductInfoCodec")).onFailure { err ->
      MainVerticle.logger.error { err.localizedMessage }
    }.onSuccess { _ ->
      MainVerticle.logger.info { "Image conversion completed" }
    }
  }
}

fun convertToWebp(name: String, extension: String, file: File) {
  val url: URL = file.toURI().toURL()
  val inputStream: InputStream?

  try {
    inputStream = url.openStream()
  } catch (e: IOException) {
    e.printStackTrace()
    return
  }

  // Change the original image to a byte array
  val byteInputStream: ByteArrayInputStream?
  val byteOutStrm = ByteArrayOutputStream()
  val originalImage = ImageIO.read(inputStream)
  ImageIO.write(originalImage, extension, byteOutStrm)
  val originalImageByteArray = byteOutStrm.toByteArray()
  byteInputStream = ByteArrayInputStream(originalImageByteArray)
  var baos: ByteArrayOutputStream? = null
  val imageOutStream: ImageOutputStream?
  try {
    // Try to change the original image as a webp image
    val image = ImageIO.read(byteInputStream)
    val writer: ImageWriter = ImageIO
      .getImageWritersByMIMEType("image/webp").next()
    baos = ByteArrayOutputStream()
    imageOutStream = ImageIO.createImageOutputStream(baos)
    writer.output = imageOutStream
    val writeParam = WebPWriteParam(writer.locale)
    writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
    writeParam.compressionType = writeParam.compressionTypes[WebPWriteParam.LOSSY_COMPRESSION]
    writeParam.compressionQuality = 0.4f
    writer.write(null, IIOImage(image, null, null), writeParam)
    imageOutStream.close()

    // Write the webp image to a file
    val byteArray = baos.toByteArray()
    val newName = File("$name.webp")
    newName.createNewFile()
    newName.writeBytes(byteArray)
  } catch (e: Exception) {
    e.printStackTrace()
  } finally {
      try {
        baos?.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
  }
}

fun convertToBasicExtensions(path: String, extension: String, validExtensions: List<String>, originalImage: File) {
  // Make a list of all extensions that have not yet been made
  val validExtensionsMutableList = validExtensions.toMutableList()
  validExtensionsMutableList.remove("webp")
  validExtensionsMutableList.remove(extension)

  for (ext in validExtensionsMutableList) {
    // Convert the image to all other formats
    val img: BufferedImage = ImageIO.read(originalImage)
    val newFile = File("$path.$ext")
    newFile.createNewFile()
    ImageIO.write(img, ext, newFile)
  }
}
