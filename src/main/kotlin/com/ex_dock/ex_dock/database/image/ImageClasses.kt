package com.ex_dock.ex_dock.database.image

class Image(
  val imageUrl: String,
  val imageName: String,
  val imageExtensions: String,
)

class ImageProduct(
  val imageUrl: String,
  val productId: Int,
)
