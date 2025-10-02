package com.ex_dock.ex_dock.database.template

import io.vertx.core.json.JsonObject

data class Template(
  val templateKey: String,
  val blockName: String,
  val templateData: String,
) {
  companion object {
    fun fromJson(json: JsonObject): Template {
      val templateKey = json.getString("_id")
      val blockName = json.getString("block_name")
      val templateData = json.getString("template_data")

      return Template(templateKey, blockName, templateData)
    }
  }

}

fun Template.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("_id", templateKey)
  document.put("block_name", blockName)
  document.put("template_data", templateData)

  return document
}
