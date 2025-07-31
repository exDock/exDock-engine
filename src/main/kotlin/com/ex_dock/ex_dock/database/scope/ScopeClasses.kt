package com.ex_dock.ex_dock.database.scope

import io.vertx.core.json.JsonObject

data class Scope(
  var scopeId: String?,
  var websiteName: String,
  var storeViewName: String
)

fun Scope.toDocument(): JsonObject {
  val document = JsonObject()

  document.put("scope_id", scopeId)
  document.put("website_name", websiteName)
  document.put("store_view_name", storeViewName)

  return document
}
