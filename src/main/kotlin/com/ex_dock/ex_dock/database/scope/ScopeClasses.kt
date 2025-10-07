package com.ex_dock.ex_dock.database.scope

import io.vertx.core.json.JsonObject

@Deprecated("Scope() is deprecated, use JsonObject instead")
data class Scope(
  var scopeId: String?,
  var websiteName: String,
  var storeViewName: String
) {
  companion object {
    fun fromJson(json: JsonObject): Scope {
      val scopeId = json.getString("_id")
      val websiteName = json.getString("website_name")
      val storeViewName = json.getString("store_view_name")

      return Scope(scopeId, websiteName, storeViewName)
    }
  }
}

fun Scope.toDocument(): JsonObject {
  val document = JsonObject()

  document.put("_id", scopeId)
  document.put("website_name", websiteName)
  document.put("store_view_name", storeViewName)

  return document
}
