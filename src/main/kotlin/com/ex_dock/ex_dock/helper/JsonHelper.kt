package com.ex_dock.ex_dock.helper

import com.google.gson.JsonElement

fun findValueByFieldName(jsonElement: JsonElement, fieldName: String): JsonElement? {
  when {
    jsonElement.isJsonObject -> {
      val jsonObject = jsonElement.asJsonObject
      // If this object contains the field, return its value
      if (jsonObject.has(fieldName)) {
        return jsonObject.get(fieldName)
      }
      // Otherwise, recursively search in nested objects
      for ((_, value) in jsonObject.entrySet()) {
        val foundValue = findValueByFieldName(value, fieldName)
        if (foundValue != null) return foundValue
      }
    }
    jsonElement.isJsonArray -> {
      val jsonArray = jsonElement.asJsonArray
      // If it's an array, check each element
      for (element in jsonArray) {
        val foundValue = findValueByFieldName(element, fieldName)
        if (foundValue != null) return foundValue
      }
    }
  }
  return null // Not found
}

fun convertJsonElement(jsonElement: JsonElement?): Any? {
  return when {
    jsonElement == null || jsonElement.isJsonNull -> null
    jsonElement.isJsonPrimitive -> {
      val primitive = jsonElement.asJsonPrimitive
      when {
        primitive.isBoolean -> primitive.asBoolean
        primitive.isNumber -> primitive.asNumber
        primitive.isString -> primitive.asString
        else -> primitive.toString() // Fallback (shouldn’t happen)
      }
    }
    jsonElement.isJsonObject -> io.vertx.core.json.JsonObject(jsonElement.asJsonObject.toString()) // Convert to Vert.x JsonObject
    jsonElement.isJsonArray -> io.vertx.core.json.JsonArray(jsonElement.asJsonArray.toString()) // Convert to Vert.x JsonArray
    else -> null
  }
}
