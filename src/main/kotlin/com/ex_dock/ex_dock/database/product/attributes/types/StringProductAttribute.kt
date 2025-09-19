package com.ex_dock.ex_dock.database.product.attributes.types

class StringProductAttribute: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    return value is String
  }
}