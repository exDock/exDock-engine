package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.category.convertToString
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.image.toDocument
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class ProductInfo(
  var productId: String?,
  var name: String,
  var shortName: String,
  var description: String,
  var shortDescription: String,
  var sku: String,
  var ean: String,
  var location: String,
  var manufacturer: String,
  var metaTitle: String,
  var metaDescription: String,
  var metaKeywords: String,
  var pageIndex: PageIndex,
  var price: Double,
  var salePrice: Double,
  var costPrice: Double,
  var taxClass: String,
  var saleDates: List<String?>,
  var categories: List<String>,
  var attributes: List<Pair<String, Any>>,
  var images: List<Image>,
)

fun ProductInfo.toDocument(): JsonObject {
  val saleDatesArray = JsonArray()
  this.saleDates.forEach { saleDate ->
    saleDatesArray.add(saleDate)
  }
  val categoryArray = JsonArray()
  this.categories.forEach { categoryId ->
    categoryArray.add(categoryId)
  }
  val attributeArray = JsonArray()
  this.attributes.forEach { (attributeName, attributeValue) ->
    attributeArray.add(
      JsonObject()
        .put("name", attributeName)
        .put("value", attributeValue)
    )
  }
  val imageArray = JsonArray()
  this.images.forEach { image ->
    imageArray.add(image.toDocument())
  }

  val document = JsonObject()
  document.put("product_id", productId)
  document.put("name", name)
  document.put("short_name", shortName)
  document.put("description", description)
  document.put("short_description", shortDescription)
  document.put("sku", sku)
  document.put("ean", ean)
  document.put("location", location)
  document.put("manufacturer", manufacturer)
  document.put("meta_title", metaTitle)
  document.put("meta_description", metaDescription)
  document.put("meta_keywords", metaKeywords)
  document.put("page_index", pageIndex.convertToString())
  document.put("price", price)
  document.put("sale_price", salePrice)
  document.put("cost_price", costPrice)
  document.put("tax_class", taxClass)
  document.put("sale_dates", saleDatesArray)
  document.put("categories", categoryArray)
  document.put("attributes", attributeArray)
  document.put("images", imageArray)

  return document
}

enum class Type(name: String) {
  BOOL("bool"),
  FLOAT("float"),
  INT("int"),
  STRING("string"),
  MONEY("money"),
  LIST("list")
}

fun Type.convertToString(): String {
  return when (this) {
    Type.STRING -> "string"
    Type.BOOL -> "bool"
    Type.FLOAT -> "float"
    Type.INT -> "int"
    Type.MONEY -> "money"
    Type.LIST -> "list"
  }
}

fun String.toType(): Type {
  return when (this) {
    "string" -> Type.STRING
    "bool" -> Type.BOOL
    "float" -> Type.FLOAT
    "int" -> Type.INT
    "money" -> Type.MONEY
    "list" -> Type.LIST
    else -> throw IllegalArgumentException("Invalid type: $this")
  }
}

fun Boolean.toInt() = if (this) 1 else 0
