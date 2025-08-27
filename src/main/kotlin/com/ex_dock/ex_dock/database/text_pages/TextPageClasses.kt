package com.ex_dock.ex_dock.database.text_pages

import com.ex_dock.ex_dock.database.category.PageIndex
import io.vertx.core.json.JsonObject

data class TextPages(
  var textPagesId: String?,
  var name: String,
  var shortText: String,
  var text: String,
  var metaTitle: String?,
  var metaDescription: String?,
  var metaKeywords: String?,
  var pageIndex: PageIndex
  ) {
  companion object {
    fun fromJson(json: JsonObject): TextPages {
      val textPagesId = json.getString("_id")
      val name = json.getString("name")
      val shortText = json.getString("short_text")
      val text = json.getString("text")
      val metaTitle = json.getString("meta_title")
      val metaDescription = json.getString("meta_description")
      val metaKeywords = json.getString("meta_keywords")
      val pageIndex = PageIndex.valueOf(json.getString("page_index"))

      return TextPages(textPagesId, name, shortText, text, metaTitle, metaDescription, metaKeywords, pageIndex)
    }
  }
}

fun TextPages.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("_id", textPagesId)
  document.put("name", name)
  document.put("short_text", shortText)
  document.put("text", text)
  document.put("meta_title", metaTitle)
  document.put("meta_description", metaDescription)
  document.put("meta_keywords", metaKeywords)
  document.put("page_index", pageIndex.toString())

  return document
}
