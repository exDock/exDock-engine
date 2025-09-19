package com.ex_dock.ex_dock.database.product.attributes.types

import java.math.BigDecimal
import java.util.Date

enum class ProductAttributeType(val typeName: String) {
  STRING(String::class.java.simpleName),
  INT(Int::class.java.simpleName),
  DOUBLE(Double::class.java.simpleName),
  BIG_DECIMAL(BigDecimal::class.java.simpleName),
  BOOLEAN(Boolean::class.java.simpleName),
  DATETIME(Date::class.java.simpleName),
  IMAGE("Image"),

  STRING_LIST("${List::class.java.simpleName}<${STRING.typeName}>"),
  IMAGE_LIST("${List::class.java.simpleName}<${IMAGE.typeName}>");

  companion object {
    fun fromString(name: String): ProductAttributeType {
      for (type in entries) if (type.typeName == name) return type

      throw IllegalArgumentException("Invalid product attribute type: $name")
    }
  }

  override fun toString(): String = typeName
}