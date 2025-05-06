package com.ex_dock.ex_dock.database.image

data class Image(
  val imageUrl: String,
  val imageName: String,
  val imageExtensions: String,
)

data class ImageProduct(
  val imageUrl: String,
  val productId: Int,
)
