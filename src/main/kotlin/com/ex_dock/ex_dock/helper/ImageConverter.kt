package com.ex_dock.ex_dock.helper

import com.luciad.imageio.webp.WebPWriteParam
import io.vertx.core.Future
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream

fun convertToWebp(name: String, extension: String, file: File): Future<Unit> {
  val url: URL = file.toURI().toURL()
  val inputStream: InputStream?

  try {
    inputStream = url.openStream()
  } catch (e: IOException) {
    e.printStackTrace()
    return Future.failedFuture(e)
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
    return Future.failedFuture(e)
  } finally {
      try {
        baos?.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
  }

  return Future.succeededFuture()
}

fun convertToBasicExtensions(path: String, extension: String, validExtensions: List<String>, originalImage: File): Future<Unit> {
  return Future.future { future ->
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

    future.succeed()
  }
}
