package com.ex_dock.ex_dock.database.image

import io.vertx.core.json.JsonObject

data class Image(
  val imageUrl: String,
  val imageName: String,
  val imageExtensions: String,
)

data class ImageProduct(
  val imageUrl: String,
  val productId: Int,
)

fun Image.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("image_url", imageUrl)
  document.put("image_name", imageName)
  document.put("image_extensions", imageExtensions)

  return document
}
