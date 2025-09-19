package com.ex_dock.ex_dock.database.product.attributes.types

import java.math.BigDecimal

class BigDecimalProductAttribute: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    return value is BigDecimal
  }
}