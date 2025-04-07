package com.ex_dock.ex_dock.database.backend_block

class BackendBlock(
  var blockId: Int?,
  var blockName: String,
  var blockType: String,
)

class BlockAttribute(
  var attributeId: String,
  var attributeName: String,
  var attributeType: String,
)

class AttributeBlock(
  var backendBlock: BackendBlock,
  var blockAttribute: BlockAttribute,
)
class BlockId(
  var blockId: Int,
  var productId: Int,
  var categoryId: Int,
)

class EavAttributeBool(
  var attributeId: String,
  var attributeKey: String,
  var value: Boolean,
)

class EavAttributeFloat(
  var attributeId: String,
  var attributeKey: String,
  var value: Float,
)

class EavAttributeString(
  var attributeId: String,
  var attributeKey: String,
  var value: String,
)

class EavAttributeInt(
  var attributeId: String,
  var attributeKey: String,
  var value: Int,
)

class EavAttributeMoney(
  var attributeId: String,
  var attributeKey: String,
  var value: Double,
)

class EavAttributeMultiSelect(
  var attributeId: String,
  var attributeKey: String,
  var value: Int,
)

class EavAttributeList(
  var attributeId: String,
  var attributeKey: String,
)

class FullBlockInfo(
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

class FullEavAttribute(
  var eavAttributeBool: List<EavAttributeBool>,
  var eavAttributeFloat: List<EavAttributeFloat>,
  var eavAttributeInt: List<EavAttributeInt>,
  var eavAttributeMoney: List<EavAttributeMoney>,
  var eavAttributeMultiSelect: List<EavAttributeMultiSelect>,
  var eavAttributeString: List<EavAttributeString>,
  var eavAttributeList: List<EavAttributeList>,
)
