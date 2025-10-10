package com.ex_dock.ex_dock.database.url

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class UrlKeys(
  var urlKey: String?,
  var upperKey: String,
  var templates: List<String>,
  var requiredParameters: List<String>
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): UrlKeys {
      val templates = jsonObject.getJsonArray("templates").map {
        it.toString()
      }
      val requiredParameters = jsonObject.getJsonArray("required_parameters").map {
        it.toString()
      }

      return UrlKeys(
        urlKey = jsonObject.getString("_id"),
        upperKey = jsonObject.getString("upper_key"),
        templates = templates,
        requiredParameters = requiredParameters
      )
    }
  }
}

fun UrlKeys.toDocument(): JsonObject {
  val templates = JsonArray()
  this.templates.forEach { template ->
    templates.add(template)
  }
  val requiredParameters = JsonArray()
  this.requiredParameters.forEach { requiredParameter ->
    requiredParameters.add(requiredParameter)
  }

  val document = JsonObject()
    .put("_id", this.urlKey)
    .put("upper_key", this.upperKey)
    .put("templates", templates)
    .put("required_parameters", requiredParameters)

  return document
}
