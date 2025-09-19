package com.ex_dock.ex_dock.database.product.attributes.types

class ImageListProductAttribute: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    if (value !is List<*>) return false

    for (element in value) if (TODO("Check if element is a reference to an image")) return false

    return true
  }
}