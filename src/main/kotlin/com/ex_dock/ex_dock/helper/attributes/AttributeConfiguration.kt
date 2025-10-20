package com.ex_dock.ex_dock.helper.attributes

import com.ex_dock.ex_dock.helper.scopes.ScopeLevel
import io.vertx.core.json.JsonObject

data class AttributeConfiguration(
  val attributeKey: String,
  val attributeName: String,
  val attributeDataType: String,
  val scopeLevel: ScopeLevel,
) {
  fun toDocument(): JsonObject {
    return JsonObject()
      .put("_id", attributeKey)
      .put("attributeName", attributeName)
      .put("attributeType", attributeDataType)
      .put("attributeScopeLevel", scopeLevel.name)
  }
}