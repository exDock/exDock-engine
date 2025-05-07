package com.ex_dock.ex_dock.helper

import com.luciad.imageio.webp.WebPWriteParam
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream

fun convertImage(path: String) {
  // Set all the path locations
  val validExtensions = listOf("png", "jpg", "webp")
  val imagePath = System.getProperty("user.dir") + "\\src\\main\\resources\\images\\"
  val pathSplit = path.split(".")
  val directorySplit = pathSplit[0].split("\\")
  val fileName = directorySplit.last()
  var directory = imagePath
  val mutableDirectorySplit = directorySplit.toMutableList()
  mutableDirectorySplit.removeAt(mutableDirectorySplit.size - 1)
  mutableDirectorySplit.forEach { part ->
    directory += part + "\\"
  }
  directory += "\\"
  val directoryPath = Paths.get(directory)
  val extension = pathSplit[1]

  // Check if the directory already exists, otherwise make the directory
  if (!Files.exists(directoryPath)) {
    File(directory).mkdirs()
  }

  val folder = File(imagePath)
  // Get the new uploaded image
  for (file in folder.listFiles()!!) {
    if (!validExtensions.contains(file.extension)) {
      val newName = File("$directory$fileName.$extension")
      file.renameTo(newName)
      convertToWebp("$directory$fileName", extension, newName)
      convertToBasicExtensions("$directory$fileName", extension, validExtensions, newName)
    }
  }

  // Delete original file if not renamed earlier
  for (file in folder.listFiles()!!) {
    if (!validExtensions.contains(file.name)) {
      file.delete()
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
