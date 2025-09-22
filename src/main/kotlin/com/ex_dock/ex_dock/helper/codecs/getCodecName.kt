package com.ex_dock.ex_dock.helper.codecs

import kotlin.reflect.KClass

fun KClass<*>.codecName(): String {
  return "${this.java.simpleName}Codec"
}

fun KClass<*>.codecListName(): String {
  return "${this.java.simpleName}ListCodec"
}
