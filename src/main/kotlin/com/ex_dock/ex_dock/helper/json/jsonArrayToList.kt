package com.ex_dock.ex_dock.helper.json

import io.vertx.core.json.JsonArray

inline fun <reified T> JsonArray.toList(): List<T> {
  val list = mutableListOf<T>()
  this.forEach { element ->
    if (element !is T) {
      throw IllegalArgumentException("JsonArray.toList(): Not all elements in are of type T")
    }
    list.add(element)
  }
  return list
}
