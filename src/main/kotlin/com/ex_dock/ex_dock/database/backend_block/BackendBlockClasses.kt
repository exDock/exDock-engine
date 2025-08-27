package com.ex_dock.ex_dock.database.backend_block

import com.ex_dock.ex_dock.database.product.Type
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class BlockAttribute(
  var attributeId: String,
  var attributeName: String,
  var attributeType: String,
)

data class BlockInfo(
  var blockId: String?,
  var pageName: String,
  var productId: String?,
  var categoryId: String?,
  var blockName: String,
  var blockType: String,
  var blockAttributes: List<BlockAttribute>
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): BlockInfo {
      val blockAttributes = emptyList<BlockAttribute>().toMutableList()
      val attributeArray = jsonObject.getJsonArray("block_attributes")

      attributeArray.forEach { attribute ->
        val jsonAttribute = attribute as JsonObject
        val attribute = BlockAttribute(
          attributeId = jsonAttribute.getString("attribute_id"),
          attributeName = jsonAttribute.getString("attribute_name"),
          attributeType = jsonAttribute.getString("attribute_type")
        )

        blockAttributes.add(attribute)
      }

      val block = BlockInfo(
        blockId = jsonObject.getString("_id"),
        pageName = jsonObject.getString("page_name"),
        productId = jsonObject.getString("product_id"),
        categoryId = jsonObject.getString("category_id"),
        blockName = jsonObject.getString("block_name"),
        blockType = jsonObject.getString("block_type"),
        blockAttributes = blockAttributes
      )

      return block
    }

    fun fromJsonList(list: List<JsonObject>): List<BlockInfo> {
      val result = mutableListOf<BlockInfo>()
      list.forEach {
        result.add(fromJson(it))
      }

      return result
    }
  }
}

fun BlockAttribute.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("attribute_id", attributeId)
  document.put("attribute_name", attributeName)
  document.put("attribute_type", attributeType)

  return document
}

fun BlockInfo.toDocument(): JsonObject {
  val blockAttributes = JsonArray()
  this.blockAttributes.forEach { attribute ->
    blockAttributes.add(attribute.toDocument())
  }

  val document = JsonObject()
  document.put("_id", blockId)
  document.put("product_id", productId)
  document.put("category_id", categoryId)
  document.put("page_name", pageName)
  document.put("block_name", blockName)
  document.put("block_type", blockType)
  document.put("block_attributes", blockAttributes)

  return document

}
