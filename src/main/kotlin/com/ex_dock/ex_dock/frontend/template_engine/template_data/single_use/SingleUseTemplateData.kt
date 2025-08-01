package com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use

import io.vertx.core.json.JsonObject

data class SingleUseTemplateData(
    val template: String,
    val templateData:Map<String, Any?>,
) {
  companion object {
    fun fromJson(json: JsonObject): SingleUseTemplateData {
      val template = json.getString("template")
      val templateData = json.getJsonObject("templateData").map

      return SingleUseTemplateData(template, templateData)
    }

  }
}

fun SingleUseTemplateData.toDocument(): JsonObject {
  val templateDataJson = JsonObject(this.templateData)

  val document = JsonObject()
    .put("template", this.template)
    .put("templateData", templateDataJson)

  return document
}
