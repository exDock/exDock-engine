package com.ex_dock.ex_dock.database.product.attributes.types

import java.math.BigDecimal
import java.util.Date

enum class ProductAttributeTypes(val typeName: String, val productAttributeType: ProductAttributeType) {
  STRING(String::class.java.simpleName, StringProductAttributeType()),
  INT(Int::class.java.simpleName, IntProductAttributeType()),
  DOUBLE(Double::class.java.simpleName, DoubleProductAttributeType()),
  BIG_DECIMAL(BigDecimal::class.java.simpleName, BigDecimalProductAttributeType()),
  BOOLEAN(Boolean::class.java.simpleName, BooleanProductAttributeType()),
  DATETIME(Date::class.java.simpleName, DateTimeProductAttributeType()),
  IMAGE("Image", ImageProductAttributeType()),

  STRING_LIST("${List::class.java.simpleName}<${STRING.typeName}>", StringListProductAttributeType()),
  IMAGE_LIST("${List::class.java.simpleName}<${IMAGE.typeName}>", ImageListProductAttributeType());

  companion object {
    fun fromString(name: String): ProductAttributeType {
      for (type in entries) if (type.typeName == name) return type.productAttributeType

      throw IllegalArgumentException("Invalid product attribute type: $name")
    }
  }

  override fun toString(): String = typeName
}