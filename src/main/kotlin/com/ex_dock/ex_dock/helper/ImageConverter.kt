package com.ex_dock.ex_dock.helper

import com.luciad.imageio.webp.WebPWriteParam
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream

fun convertImage(path: String, imageBytes: String) {
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

  val extension = pathSplit.last().split(".")[1]
  val fileName: String = pathSplit.last().split(".")[0]
  for (pathItem in pathSplit) {
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
  if (extension == "jpg" || extension == "jpeg") {
    validExtensions.remove("jpg")
    validExtensions.remove("jpeg")
  } else {
    validExtensions.remove(extension)
    validExtensions.remove("jpeg")
  }

  // Get the new uploaded image
  convertToWebp("$directory\\$fileName", extension, newFile)
  convertToBasicExtensions("$directory\\$fileName", extension, validExtensions, newFile)
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
