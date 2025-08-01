package com.ex_dock.ex_dock.database.url

import io.vertx.core.json.JsonObject

data class UrlKeys(
  var urlKey: String,
  var upperKey: String,
  var requestedId: String,
  var pageType: PageType
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): UrlKeys {
      return UrlKeys(
        urlKey = jsonObject.getString("_id"),
        upperKey = jsonObject.getString("upper_key"),
        requestedId = jsonObject.getString("requested_id"),
        pageType = jsonObject.getString("page_type").toPageType()
      )
    }
  }
}

fun UrlKeys.toDocument(): JsonObject {
  val document = JsonObject()
    .put("_id", this.urlKey)
    .put("upper_key", this.upperKey)
    .put("requested_id", this.requestedId)
    .put("page_type", this.pageType.convertToString())

  return document
}

enum class PageType(name: String) {
  PRODUCT("product"),
  CATEGORY("category"),
  TEXT_PAGE("text_page")
}

fun PageType.convertToString(): String {
  return when (this) {
    PageType.TEXT_PAGE -> "text_page"
    PageType.CATEGORY -> "category"
    PageType.PRODUCT -> "product"
  }
}

fun String.toPageType(): PageType {
  return when (this) {
    "text_page" -> PageType.TEXT_PAGE
    "category" -> PageType.CATEGORY
    "product" -> PageType.PRODUCT
    else -> throw IllegalArgumentException("Invalid page type: $this")
  }
}
