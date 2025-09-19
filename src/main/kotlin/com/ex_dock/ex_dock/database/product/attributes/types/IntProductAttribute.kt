package com.ex_dock.ex_dock.database.product.attributes.types

class IntProductAttribute: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    return value is Int
  }
}