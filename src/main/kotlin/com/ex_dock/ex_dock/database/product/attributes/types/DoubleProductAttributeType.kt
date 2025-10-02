package com.ex_dock.ex_dock.database.product.attributes.types

class DoubleProductAttributeType: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    return value is Double
  }
}