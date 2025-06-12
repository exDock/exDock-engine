package com.ex_dock.ex_dock.database.backend_block

data class BackendBlock(
  var blockId: Int?,
  var blockName: String,
  var blockType: String,
)

data class BlockAttribute(
  var attributeId: String,
  var attributeName: String,
  var attributeType: String,
)

data class AttributeBlock(
  var backendBlock: BackendBlock,
  var blockAttribute: BlockAttribute,
)

data class BlockId(
  var blockId: Int,
  var productId: Int,
  var categoryId: Int,
)

data class EavAttributeBool(
  var attributeId: String,
  var attributeKey: String,
  var value: Boolean,
)

data class EavAttributeFloat(
  var attributeId: String,
  var attributeKey: String,
  var value: Float,
)

data class EavAttributeString(
  var attributeId: String,
  var attributeKey: String,
  var value: String,
)

data class EavAttributeInt(
  var attributeId: String,
  var attributeKey: String,
  var value: Int,
)

data class EavAttributeMoney(
  var attributeId: String,
  var attributeKey: String,
  var value: Double,
)

data class EavAttributeMultiSelect(
  var attributeId: String,
  var attributeKey: String,
  var value: Int,
)

data class EavAttributeList(
  var attributeId: String,
  var attributeKey: String,
)

data class FullBlockInfo(
  var blockId: BlockId,
  var backendBlock: BackendBlock,
  var blockAttributes: List<BlockAttribute>,
  var eavAttributeBool: List<EavAttributeBool>,
  var eavAttributeFloat: List<EavAttributeFloat>,
  var eavAttributeInt: List<EavAttributeInt>,
  var eavAttributeMoney: List<EavAttributeMoney>,
  var eavAttributeMultiSelect: List<EavAttributeMultiSelect>,
  var eavAttributeString: List<EavAttributeString>,
  var eavAttributeList: List<EavAttributeList>,
)

data class FullEavAttribute(
  var eavAttributeBool: List<EavAttributeBool>,
  var eavAttributeFloat: List<EavAttributeFloat>,
  var eavAttributeInt: List<EavAttributeInt>,
  var eavAttributeMoney: List<EavAttributeMoney>,
  var eavAttributeMultiSelect: List<EavAttributeMultiSelect>,
  var eavAttributeString: List<EavAttributeString>,
  var eavAttributeList: List<EavAttributeList>,
)
