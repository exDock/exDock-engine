package com.ex_dock.ex_dock.helper

import com.google.gson.JsonElement

fun JsonElement.findValueByFieldName(fieldName: String): JsonElement? {
  when {
    this.isJsonObject -> {
      val jsonObject = this.asJsonObject
      // If this object contains the field, return its value
      if (jsonObject.has(fieldName)) {
        return jsonObject.get(fieldName)
      }
      // Otherwise, recursively search in nested objects
      for ((_, value) in jsonObject.entrySet()) {
        val foundValue = this.findValueByFieldName(fieldName)
        if (foundValue != null) return foundValue
      }
    }
    this.isJsonArray -> {
      val jsonArray = this.asJsonArray
      // If it's an array, check each element
      for (element in jsonArray) {
        val foundValue = element.findValueByFieldName(fieldName)
        if (foundValue != null) return foundValue
      }
    }
  }
  return null // Not found
}

fun JsonElement?.convertJsonElement(): Any? {
  return when {
    this == null || this.isJsonNull -> null
    this.isJsonPrimitive -> {
      val primitive = this.asJsonPrimitive
      when {
        primitive.isBoolean -> primitive.asBoolean
        primitive.isNumber -> primitive.asNumber
        primitive.isString -> primitive.asString
        else -> primitive.toString() // Fallback (shouldn’t happen)
      }
    }
    this.isJsonObject -> io.vertx.core.json.JsonObject(this.asJsonObject.toString()) // Convert to Vert.x JsonObject
    this.isJsonArray -> io.vertx.core.json.JsonArray(this.asJsonArray.toString()) // Convert to Vert.x JsonArray
    else -> null
  }
}
