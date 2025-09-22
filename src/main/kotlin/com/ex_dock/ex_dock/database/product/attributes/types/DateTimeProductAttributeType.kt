package com.ex_dock.ex_dock.database.product.attributes.types

import java.util.Date

class DateTimeProductAttributeType: ProductAttributeType() {
  override fun isType(value: Any): Boolean {
    return value is Date
  }
}