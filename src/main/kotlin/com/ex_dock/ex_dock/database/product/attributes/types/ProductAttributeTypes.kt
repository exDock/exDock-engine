package com.ex_dock.ex_dock.database.product.attributes.types

import java.math.BigDecimal
import java.util.Date

enum class ProductAttributeTypes(val typeName: String, val productAttributeType: ProductAttributeType) {
  STRING(String::class.java.simpleName, StringProductAttribute()),
  INT(Int::class.java.simpleName, IntProductAttribute()),
  DOUBLE(Double::class.java.simpleName, DoubleProductAttribute()),
  BIG_DECIMAL(BigDecimal::class.java.simpleName, BigDecimalProductAttribute()),
  BOOLEAN(Boolean::class.java.simpleName, BooleanProductAttribute()),
  DATETIME(Date::class.java.simpleName, DateTimeProductAttribute()),
  IMAGE("Image", ImageProductAttribute()),

  STRING_LIST("${List::class.java.simpleName}<${STRING.typeName}>", StringListProductAttribute()),
  IMAGE_LIST("${List::class.java.simpleName}<${IMAGE.typeName}>", ImageProductAttribute());

  companion object {
    fun fromString(name: String): ProductAttributeType {
      for (type in entries) if (type.typeName == name) return type.productAttributeType

      throw IllegalArgumentException("Invalid product attribute type: $name")
    }
  }

  override fun toString(): String = typeName
}