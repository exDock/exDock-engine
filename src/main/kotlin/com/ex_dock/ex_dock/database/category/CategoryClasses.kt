package com.ex_dock.ex_dock.database.category

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

//import com.ex_dock.ex_dock.database.product.Products

data class CategoryInfo(
  var categoryId: String?,
  var upperCategory: String?,
  var name: String,
  var shortDescription: String,
  var description: String,
  var metaTitle: String?,
  var metaDescription: String?,
  var metaKeywords: String?,
  var pageIndex: PageIndex,
  var products: List<String>
)

fun CategoryInfo.toDocument(): JsonObject {
  val productIds = JsonArray()
  this.products.forEach { productId ->
    productIds.add(productId)
  }

  val document = JsonObject()
  document.put("category_id", categoryId)
  document.put("upper_category", upperCategory)
  document.put("name", name)
  document.put("short_description", shortDescription)
  document.put("description", description)
  document.put("meta_title", metaTitle)
  document.put("meta_description", metaDescription)
  document.put("meta_keywords", metaKeywords)
  document.put("page_index", pageIndex.convertToString())
  document.put("products", productIds)

  return document
}

enum class PageIndex(pIndex: String) {
  IndexFollow("index, follow"),
  IndexNoFollow("index, nofollow"),
  NoIndexFollow("noindex, follow"),
  NoIndexNoFollow("noindex, nofollow");
}

fun String.toPageIndex(): PageIndex {
  when (this) {
    "index, follow" -> return PageIndex.IndexFollow
    "index, nofollow" -> return PageIndex.IndexNoFollow
    "noindex, follow" -> return PageIndex.NoIndexFollow
    "noindex, nofollow" -> return PageIndex.NoIndexNoFollow
  }

  return PageIndex.NoIndexNoFollow
}

fun PageIndex.convertToString(): String {
  return when (this) {
    PageIndex.IndexFollow -> "index, follow"
    PageIndex.IndexNoFollow -> "index, nofollow"
    PageIndex.NoIndexFollow -> "noindex, follow"
    PageIndex.NoIndexNoFollow -> "noindex, nofollow"
  }
}
