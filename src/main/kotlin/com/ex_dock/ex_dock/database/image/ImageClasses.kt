package com.ex_dock.ex_dock.database.image

import io.vertx.core.json.JsonObject

data class Image(
  val imageUrl: String,
  val imageName: String,
  val imageExtensions: String,
) {
  companion object {
    fun fromJson(json: JsonObject): Image {
      val imageUrl = json.getString("image_url")
      val imageName = json.getString("image_name")
      val imageExtensions = json.getString("image_extensions")

      return Image(imageUrl, imageName, imageExtensions)
    }
  }

}

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
