package com.ex_dock.ex_dock.database.product.attributes.types

class StringListProductAttribute: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    if (value !is List<*>) return false

    for (element in value) if (element !is String) return false

    return true
  }
}