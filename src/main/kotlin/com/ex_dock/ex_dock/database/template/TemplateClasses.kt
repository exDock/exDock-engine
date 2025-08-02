package com.ex_dock.ex_dock.database.template

import io.vertx.core.json.JsonObject

data class Template(
  val templateKey: String,
  val blockName: String,
  val templateData: String,
  val dataString: String,
) {
  companion object {
    fun fromJson(json: JsonObject): Template {
      val templateKey = json.getString("template_key")
      val blockName = json.getString("block_name")
      val templateData = json.getString("template_data")
      val dataString = json.getString("data_string")

      return Template(templateKey, blockName, templateData, dataString)
    }
  }

}

fun Template.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("template_key", templateKey)
  document.put("block_name", blockName)
  document.put("template_data", templateData)
  document.put("data_string", dataString)

  return document
}
