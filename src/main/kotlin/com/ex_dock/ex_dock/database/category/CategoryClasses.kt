package com.ex_dock.ex_dock.database.category

import com.ex_dock.ex_dock.database.product.Products

data class Categories(
  var categoryId: Int?,
  var upperCategory: Int?,
  var name: String,
  var shortDescription: String,
  var description: String
)

data class CategoriesProducts(
  val categoryId: Categories,
  val productId: Products
)

data class CategoriesSeo(
  val categoryId: Int,
  var metaTitle: String?,
  var metaDescription: String?,
  var metaKeywords: String?,
  var pageIndex: PageIndex
)

data class FullCategoryInfo(
  val categories: Categories,
  val categoriesSeo: CategoriesSeo
)

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
