package com.ex_dock.ex_dock.database.product.attributes.types

abstract class ProductAttributeType {
  abstract fun isType(value: Any): Boolean
}