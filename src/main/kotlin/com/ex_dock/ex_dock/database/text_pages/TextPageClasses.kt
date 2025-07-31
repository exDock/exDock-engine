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
  )

fun TextPages.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("text_pages_id", textPagesId)
  document.put("name", name)
  document.put("short_text", shortText)
  document.put("text", text)
  document.put("meta_title", metaTitle)
  document.put("meta_description", metaDescription)
  document.put("meta_keywords", metaKeywords)
  document.put("page_index", pageIndex.toString())

  return document
}
