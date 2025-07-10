package com.ex_dock.ex_dock.database.backend_block

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class BackendBlockJdbcVerticle : VerticleBase() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")
  private val backendBlockDeliveryOptions = DeliveryOptions().setCodecName("BackendBlockCodec")
  private val blockAttributeDeliveryOptions = DeliveryOptions().setCodecName("BlockAttributeCodec")
  private val fullBlockInfoDeliveryOptions = DeliveryOptions().setCodecName("FullBlockInfoCodec")
  private val eavBoolDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeBoolCodec")
  private val eavFloatDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeFloatCodec")
  private val eavIntDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeIntCodec")
  private val eavMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeMoneyCodec")
  private val eavMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeMultiSelectCodec")
  private val eavStringDeliveryOptions = DeliveryOptions().setCodecName("EavAttributeStringCodec")

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for backend blocks
    getAllBackendBlocks()
    getBackendBlockById()
    createBackendBlock()
    editBackendBlock()
    deleteBackendBlock()

    // Initialize all eventbus connections for block attributes
    getAllBlockAttributes()
    getBlockAttributeById()
    createBlockAttribute()
    editBlockAttribute()
    deleteBlockAttribute()

    // Initialize all eventbus connections for EAV attributes - Bool
    getAllEavAttributeBool()
    getEavAttributeBoolById()
    createEavAttributeBool()
    editEavAttributeBool()
    deleteEavAttributeBool()

    // Initialize all eventbus connections for EAV attributes - Float
    createEavAttributeFloat()
    editEavAttributeFloat()
    deleteEavAttributeFloat()

    // Initialize all eventbus connections for EAV attributes - Int
    createEavAttributeInt()
    editEavAttributeInt()
    deleteEavAttributeInt()

    // Initialize all eventbus connections for EAV attributes - Money
    createEavAttributeMoney()
    editEavAttributeMoney()
    deleteEavAttributeMoney()

    // Initialize all eventbus connections for EAV attributes - MultiSelect
    createEavAttributeMultiSelect()
    editEavAttributeMultiSelect()
    deleteEavAttributeMultiSelect()

    // Initialize all eventbus connections for EAV attributes - String
    createEavAttributeString()
    editEavAttributeString()
    deleteEavAttributeString()

    // Initialize all eventbus connections for getting Full Block Information
    getFullBlockInfoById()
    getFullBackendBlock()
    getFullBackendBlockByBlockNames()

    return Future.succeededFuture<Unit>()
  }

  /**
   * Retrieves all backend blocks from the database and sends them as a JSON object to the specified EventBus address.
   */
  private fun getAllBackendBlocks() {
    val getAllBackendBlocksConsumer = eventBus.localConsumer<Unit>("process.backend_block.getAll")
    getAllBackendBlocksConsumer.handler { message ->
      val query = "SELECT * FROM backend_block"
      val rowsFuture = client.preparedQuery(query).execute()
      val blockList: MutableList<BackendBlock> = emptyList<BackendBlock>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            blockList.add(row.makeBackendBlock())
          }
        }
        message.reply(blockList, listDeliveryOptions)
      }
    }
  }

  /**
   * Retrieves a backend block from the database based on the provided block ID.
   */
  private fun getBackendBlockById() {
    val getBackendBlockByIdConsumer = eventBus.localConsumer<Int>("process.backend_block.getById")
    getBackendBlockByIdConsumer.handler { message ->
      val query = "SELECT * FROM backend_block WHERE block_id = ?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          message.reply(res.first().makeBackendBlock(), backendBlockDeliveryOptions)
        } else {
          message.reply("No backend block found")
        }
      }
    }
  }

  /**
   * Create a new backend block entry in the database
   */
  private fun createBackendBlock() {
    val createBackendBlockConsumer = eventBus.localConsumer<BackendBlock>("process.backend_block.create")
    createBackendBlockConsumer.handler { message ->
      val query = "INSERT INTO backend_block (block_name, block_type) VALUES (?,?)"
      val block = message.body()

      val queryTuple = Tuple.of(
        block.blockName,
        block.blockType
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res ->
        val blockId = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        block.blockId = blockId
        message.reply(block, backendBlockDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing backend block in the database
   */
  private fun editBackendBlock() {
    val editBackendBlockConsumer = eventBus.localConsumer<BackendBlock>("process.backend_block.edit")
    editBackendBlockConsumer.handler { message ->
      val query = "UPDATE backend_block SET block_name = ?, block_type = ? WHERE block_id = ?"
      val block = message.body()

      val queryTuple = Tuple.of(
        block.blockName,
        block.blockType,
        block.blockId
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(block, backendBlockDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing backend block in the database
   */
  private fun deleteBackendBlock() {
    val deleteBackendBlockConsumer = eventBus.localConsumer<Int>("process.backend_block.delete")
    deleteBackendBlockConsumer.handler { message ->
      val query = "DELETE FROM backend_block WHERE block_id = ?"
      val id = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("Backend block deleted successfully!")
      }
    }
  }

  /**
   * Retrieves all block attributes from the database
   */
  private fun getAllBlockAttributes() {
    val getAllBlockAttributesConsumer = eventBus.localConsumer<Unit>("process.block_attribute.getAll")
    getAllBlockAttributesConsumer.handler { message ->
      val query = "SELECT * FROM block_attributes"
      val rowsFuture = client.preparedQuery(query).execute()
      val attributeList: MutableList<BlockAttribute> = emptyList<BlockAttribute>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            attributeList.add(row.makeBlockAttribute())
          }
        }
        message.reply(attributeList, listDeliveryOptions)
      }
    }
  }

  /**
   * Retrieves a block attribute from the database based on the provided attribute ID.
   */
  private fun getBlockAttributeById() {
    val getBlockAttributeByIdConsumer = eventBus.localConsumer<String>("process.block_attribute.getById")
    getBlockAttributeByIdConsumer.handler { message ->
      val query = "SELECT * FROM block_attributes WHERE attribute_id = ?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          message.reply(res.first().makeBlockAttribute(), blockAttributeDeliveryOptions)
        } else {
          message.reply("No block attribute found")
        }
      }
    }
  }

  /**
   * Create a new block attribute entry in the database
   */
  private fun createBlockAttribute() {
    val createBlockAttributeConsumer = eventBus.localConsumer<BlockAttribute>("process.block_attribute.create")
    createBlockAttributeConsumer.handler { message ->
      val query = "INSERT INTO block_attributes (attribute_id, attribute_name, attribute_type) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeName,
        attribute.attributeType
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, blockAttributeDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing block attribute in the database
   */
  private fun editBlockAttribute() {
    val editBlockAttributeConsumer = eventBus.localConsumer<BlockAttribute>("process.block_attribute.edit")
    editBlockAttributeConsumer.handler { message ->
      val query = "UPDATE block_attributes SET attribute_name = ?, attribute_type = ? WHERE attribute_id = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeName,
        attribute.attributeType,
        attribute.attributeId
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, blockAttributeDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing block attribute in the database
   */
  private fun deleteBlockAttribute() {
    val deleteBlockAttributeConsumer = eventBus.localConsumer<String>("process.block_attribute.delete")
    deleteBlockAttributeConsumer.handler { message ->
      val query = "DELETE FROM block_attributes WHERE attribute_id = ?"
      val id = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("Block attribute deleted successfully!")
      }
    }
  }

  // EAV BOOLEAN ATTRIBUTES
  /**
   * Retrieves all EAV boolean attributes from the database
   */
  private fun getAllEavAttributeBool() {
    val getAllEavAttributeBoolConsumer = eventBus.localConsumer<Unit>("process.eav_attribute_bool.getAll")
    getAllEavAttributeBoolConsumer.handler { message ->
      val query = "SELECT * FROM eav_attribute_bool"
      val rowsFuture = client.preparedQuery(query).execute()
      val attributeList: MutableList<EavAttributeBool> = emptyList<EavAttributeBool>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            attributeList.add(row.makeEavAttributeBool())
          }
        }
        message.reply(attributeList, listDeliveryOptions)
      }
    }
  }

  /**
   * Retrieves an EAV boolean attribute by ID and key
   */
  private fun getEavAttributeBoolById() {
    val getEavAttributeBoolByIdConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_bool.getById")
    getEavAttributeBoolByIdConsumer.handler { message ->
      val query = "SELECT * FROM eav_attribute_bool WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          message.reply(res.first().makeEavAttributeBool(), eavBoolDeliveryOptions)
        } else {
          message.reply("No EAV boolean attribute found")
        }
      }
    }
  }

  /**
   * Create a new EAV boolean attribute entry in the database
   */
  /**
   * Create a new EAV boolean attribute entry in the database
   */
  private fun createEavAttributeBool() {
    val createEavAttributeBoolConsumer = eventBus.localConsumer<EavAttributeBool>("process.eav_attribute_bool.create")
    createEavAttributeBoolConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_bool (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
        message.reply(attribute, eavBoolDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV boolean attribute in the database
   */
  private fun editEavAttributeBool() {
    val editEavAttributeBoolConsumer = eventBus.localConsumer<EavAttributeBool>("process.eav_attribute_bool.edit")
    editEavAttributeBoolConsumer.handler { message ->
      val query = "UPDATE eav_attribute_bool SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavBoolDeliveryOptions)
      }
    }
  }

  private fun deleteEavAttributeBool() {
    val deleteEavAttributeBoolConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_bool.delete")
    deleteEavAttributeBoolConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_bool WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV bool attribute deleted successfully!")
      }
    }
  }

  /**
   * Create a new EAV float attribute entry in the database
   */
  private fun createEavAttributeFloat() {
    val createEavAttributeFloatConsumer = eventBus.localConsumer<EavAttributeFloat>("process.eav_attribute_float.create")
    createEavAttributeFloatConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_float (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavFloatDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV float attribute in the database
   */
  private fun editEavAttributeFloat() {
    val editEavAttributeFloatConsumer = eventBus.localConsumer<EavAttributeFloat>("process.eav_attribute_float.edit")
    editEavAttributeFloatConsumer.handler { message ->
      val query = "UPDATE eav_attribute_float SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavFloatDeliveryOptions)
      }
    }
  }

  private fun deleteEavAttributeFloat() {
    val deleteEavAttributeFloatConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_float.delete")
    deleteEavAttributeFloatConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_float WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV float attribute deleted successfully!")
      }
    }
  }

  /**
   * Create a new EAV int attribute entry in the database
   */
  private fun createEavAttributeInt() {
    val createEavAttributeIntConsumer = eventBus.localConsumer<EavAttributeInt>("process.eav_attribute_int.create")
    createEavAttributeIntConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_int (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavIntDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV int attribute in the database
   */
  private fun editEavAttributeInt() {
    val editEavAttributeIntConsumer = eventBus.localConsumer<EavAttributeInt>("process.eav_attribute_int.edit")
    editEavAttributeIntConsumer.handler { message ->
      val query = "UPDATE eav_attribute_int SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavIntDeliveryOptions)
      }
    }
  }

  private fun deleteEavAttributeInt() {
    val deleteEavAttributeIntConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_int.delete")
    deleteEavAttributeIntConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_int WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV int attribute deleted successfully!")
      }
    }
  }

  /**
   * Create a new EAV money attribute entry in the database
   */
  private fun createEavAttributeMoney() {
    val createEavAttributeMoneyConsumer = eventBus.localConsumer<EavAttributeMoney>("process.eav_attribute_money.create")
    createEavAttributeMoneyConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_money (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavMoneyDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV money attribute in the database
   */
  private fun editEavAttributeMoney() {
    val editEavAttributeMoneyConsumer = eventBus.localConsumer<EavAttributeMoney>("process.eav_attribute_money.edit")
    editEavAttributeMoneyConsumer.handler { message ->
      val query = "UPDATE eav_attribute_money SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavMoneyDeliveryOptions)
      }
    }
  }

  private fun deleteEavAttributeMoney() {
    val deleteEavAttributeMoneyConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_money.delete")
    deleteEavAttributeMoneyConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_money WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV money attribute deleted successfully!")
      }
    }
  }

  /**
   * Create a new EAV multi-select attribute entry in the database
   */
  private fun createEavAttributeMultiSelect() {
    val createEavAttributeMultiSelectConsumer = eventBus.localConsumer<EavAttributeMultiSelect>("process.eav_attribute_multi_select.create")
    createEavAttributeMultiSelectConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_multi_select (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavMultiSelectDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV multi-select attribute in the database
   */
  private fun editEavAttributeMultiSelect() {
    val editEavAttributeMultiSelectConsumer = eventBus.localConsumer<EavAttributeMultiSelect>("process.eav_attribute_multi_select.edit")
    editEavAttributeMultiSelectConsumer.handler { message ->
      val query = "UPDATE eav_attribute_multi_select SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavMultiSelectDeliveryOptions)
      }
    }
  }

  private fun deleteEavAttributeMultiSelect() {
    val deleteEavAttributeMultiSelectConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_multi_select.delete")
    deleteEavAttributeMultiSelectConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_multi_select WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV multi-select attribute deleted successfully!")
      }
    }
  }

  /**
   * Create a new EAV string attribute entry in the database
   */
  private fun createEavAttributeString() {
    val createEavAttributeStringConsumer = eventBus.localConsumer<EavAttributeString>("process.eav_attribute_string.create")
    createEavAttributeStringConsumer.handler { message ->
      val query = "INSERT INTO eav_attribute_string (attribute_id, attribute_key, value) VALUES (?,?,?)"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.attributeId,
        attribute.attributeKey,
        attribute.value
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavStringDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing EAV string attribute in the database
   */
  private fun editEavAttributeString() {
    val editEavAttributeStringConsumer = eventBus.localConsumer<EavAttributeString>("process.eav_attribute_string.edit")
    editEavAttributeStringConsumer.handler { message ->
      val query = "UPDATE eav_attribute_string SET value = ? WHERE attribute_id = ? AND attribute_key = ?"
      val attribute = message.body()

      val queryTuple = Tuple.of(
        attribute.value,
        attribute.attributeId,
        attribute.attributeKey
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(attribute, eavStringDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing EAV string attribute in the database
   */
  private fun deleteEavAttributeString() {
    val deleteEavAttributeStringConsumer = eventBus.localConsumer<Pair<String, String>>("process.eav_attribute_string.delete")
    deleteEavAttributeStringConsumer.handler { message ->
      val query = "DELETE FROM eav_attribute_string WHERE attribute_id = ? AND attribute_key = ?"
      val (attributeId, attributeKey) = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(attributeId, attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("EAV string attribute deleted successfully!")
      }
    }
  }

  /**
   * Get all data from backend blocks with complete information including all attribute types
   */
  private fun getFullBackendBlock() {
    val getFullBackendBlockConsumer = eventBus.localConsumer<Unit>("process.backend_block.getAllFullInfo")
    getFullBackendBlockConsumer.handler { message ->
      val query = """
      SELECT bb.block_id, bb.block_name, bb.block_type,
             ba.attribute_id, ba.attribute_name, ba.attribute_type
      FROM backend_block bb
      LEFT JOIN attribute_block bab ON bb.block_id = bab.block_id
      LEFT JOIN block_attributes ba ON bab.attribute_id = ba.attribute_id
    """

      val rowsFuture = client.preparedQuery(query).execute()
      val blockInfoMap = mutableMapOf<Int, MutableList<Pair<BackendBlock, BlockAttribute>>>()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            val blockId = row.getInteger("block_id")
            val backendBlock = row.makeBackendBlock()

            // Check if the attribute_id is null (in case of LEFT JOIN with no match)
            val attributeId = row.getString("attribute_id")
            val blockAttribute = if (attributeId != null) {
              row.makeBlockAttribute()
            } else {
              null
            }

            val pair = if (blockAttribute != null) {
              Pair(backendBlock, blockAttribute)
            } else {
              null
            }

            if (blockInfoMap.containsKey(blockId)) {
              if (pair != null) {
                blockInfoMap[blockId]?.add(pair)
              }
            } else {
              blockInfoMap[blockId] = mutableListOf()
              if (pair != null) {
                blockInfoMap[blockId]?.add(pair)
              }
            }
          }
        }

        // Now fetch EAV attributes for each block
        val fullBlockInfoList = mutableListOf<FullBlockInfo>()

        for ((blockId, blockInfoList) in blockInfoMap) {
          // Get the first pair to extract the BackendBlock
          val backendBlock = blockInfoList.firstOrNull()?.first ?: continue

          // Extract all block attributes
          val blockAttributes = blockInfoList.mapNotNull { it.second }

          // Create a BlockId object (assuming it's constructed with defaults)
          val blockIdObj = BlockId(blockId = blockId, productId = 0, categoryId = 0)

          // Fetch EAV attributes for this block
          fetchEavAttributes(blockId).onComplete { ar ->
            if (ar.succeeded()) {
              val result = ar.result()
              val eavBool = result.eavAttributeBool
              val eavFloat = result.eavAttributeFloat
              val eavInt = result.eavAttributeInt
              val eavMoney = result.eavAttributeMoney
              val eavMultiSelect = result.eavAttributeMultiSelect
              val eavString = result.eavAttributeString
              val eavList = result.eavAttributeList

              val fullBlockInfo = FullBlockInfo(
                blockId = blockIdObj,
                backendBlock = backendBlock,
                blockAttributes = blockAttributes,
                eavAttributeBool = eavBool,
                eavAttributeFloat = eavFloat,
                eavAttributeInt = eavInt,
                eavAttributeMoney = eavMoney,
                eavAttributeMultiSelect = eavMultiSelect,
                eavAttributeString = eavString,
                eavAttributeList = eavList,
              )

              fullBlockInfoList.add(fullBlockInfo)

              // If we've processed all blocks, send the reply
              if (fullBlockInfoList.size == blockInfoMap.size) {
                message.reply(fullBlockInfoList, listDeliveryOptions)
              }
            } else {
              println("Failed to fetch EAV attributes: ${ar.cause()}")
              message.reply("Failed to fetch EAV attributes: ${ar.cause()}")
            }
          }
        }

        // If no blocks were found, send an empty list
        if (blockInfoMap.isEmpty()) {
          message.reply(fullBlockInfoList, listDeliveryOptions)
        }
      }
    }
  }

  /**
   * Get the full block information for a specific block ID
   */
  private fun getFullBlockInfoById() {
    val getFullBlockInfoByIdConsumer = eventBus.localConsumer<Int>("process.backend_block.getFullInfoById")
    getFullBlockInfoByIdConsumer.handler { message ->
      val blockId = message.body()

      val query = """
      SELECT bb.block_id, bb.block_name, bb.block_type,
             ba.attribute_id, ba.attribute_name, ba.attribute_type
      FROM attribute_block bab
      LEFT JOIN backend_block bb ON bb.block_id = bab.block_id
      LEFT JOIN block_attributes ba ON bab.attribute_id = ba.attribute_id
      WHERE bab.block_id = ?
    """

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(blockId))
      val blockAttributes = mutableListOf<BlockAttribute>()
      var backendBlock: BackendBlock? = null

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            // Initialize the backendBlock if it's null
            if (backendBlock == null) {
              backendBlock = row.makeBackendBlock()
            }

            // Check if the attribute_id is null (in case of LEFT JOIN with no match)
            val attributeId = row.getString("attribute_id")
            if (attributeId != null) {
              blockAttributes.add(row.makeBlockAttribute())
            }
          }

          // Create a BlockId object
          val blockIdObj = BlockId(blockId = blockId, productId = 0, categoryId = 0)

          // Fetch EAV attributes for this block
          fetchEavAttributes(blockId).onComplete { ar ->
            if (ar.succeeded()) {
              val result = ar.result()
              val eavBool = result.eavAttributeBool
              val eavFloat = result.eavAttributeFloat
              val eavInt = result.eavAttributeInt
              val eavMoney = result.eavAttributeMoney
              val eavMultiSelect = result.eavAttributeMultiSelect
              val eavString = result.eavAttributeString
              val eavList = result.eavAttributeList

              val fullBlockInfo = FullBlockInfo(
                blockId = blockIdObj,
                backendBlock = backendBlock!!,
                blockAttributes = blockAttributes,
                eavAttributeBool = eavBool,
                eavAttributeFloat = eavFloat,
                eavAttributeInt = eavInt,
                eavAttributeMoney = eavMoney,
                eavAttributeMultiSelect = eavMultiSelect,
                eavAttributeString = eavString,
                eavAttributeList = eavList
              )

              message.reply(fullBlockInfo, fullBlockInfoDeliveryOptions)
            } else {
              println("Failed to fetch EAV attributes: ${ar.cause()}")
              message.reply("Failed to fetch EAV attributes: ${ar.cause()}")
            }
          }
        } else {
          message.reply(mutableListOf<FullBlockInfo>(), listDeliveryOptions)
        }
      }
    }
  }

  private fun getFullBackendBlockByBlockNames() {
    val getFullBackendBlockConsumer = eventBus.localConsumer<String>("process.backend_block.getAllFullInfoByBlockNames")
    getFullBackendBlockConsumer.handler { message ->
      val pageQuery = """
        SELECT * FROM page_block WHERE page_name = ?
      """.trimIndent()
      val pageRowsFuture = client.preparedQuery(pageQuery).execute(Tuple.of(message.body()))
      pageRowsFuture.onFailure {
        println("Failed to execute query: $it")
        message.reply("Failed to execute query: $it")
      }.onComplete { rows ->
        val pageBlockList = mutableListOf<Int>()
        rows.result().forEach { row ->
          pageBlockList.add(row.getInteger("block_id"))
        }
        val query = """
            SELECT bb.block_id, bb.block_name, bb.block_type,
                  ba.attribute_id, ba.attribute_name, ba.attribute_type
            FROM backend_block bb
            LEFT JOIN attribute_block bab ON bb.block_id = bab.block_id
            LEFT JOIN block_attributes ba ON bab.attribute_id = ba.attribute_id
            LEFT JOIN public.attribute_list al on ba.attribute_id = al.attribute_id
            WHERE bb.block_id = ANY (?)
        """
        val rowsFuture = client.preparedQuery(query).execute(Tuple.of(pageBlockList.toIntArray()))
        val blockInfoMap = mutableMapOf<Int, MutableList<Pair<BackendBlock, BlockAttribute>>>()
        rowsFuture.onFailure { res ->
          println("Failed to execute query: $res")
          message.reply("Failed to execute query: $res")
        }.onSuccess { res: RowSet<Row> ->
          if (res.size() > 0) {
            res.forEach { row ->
              val blockId = row.getInteger("block_id")
              val backendBlock = row.makeBackendBlock()

              // Check if the attribute_id is null (in case of LEFT JOIN with no match)
              val attributeId = row.getString("attribute_id")
              val blockAttribute = if (attributeId != null) {
                row.makeBlockAttribute()
              } else {
                null
              }

              val pair = if (blockAttribute != null) {
                Pair(backendBlock, blockAttribute)
              } else {
                null
              }

              if (blockInfoMap.containsKey(blockId)) {
                if (pair != null) {
                  blockInfoMap[blockId]?.add(pair)
                }
              } else {
                blockInfoMap[blockId] = mutableListOf()
                if (pair != null) {
                  blockInfoMap[blockId]?.add(pair)
                }
              }
            }
          }
          // Now fetch EAV attributes for each block
          val fullBlockInfoList = mutableListOf<FullBlockInfo>()

          for ((blockId, blockInfoList) in blockInfoMap) {
            // Get the first pair to extract the BackendBlock
            val backendBlock = blockInfoList.firstOrNull()?.first ?: continue

            // Extract all block attributes
            val blockAttributes = blockInfoList.mapNotNull { it.second }

            // Create a BlockId object (assuming it's constructed with defaults)
            val blockIdObj = BlockId(blockId = blockId, productId = 0, categoryId = 0)

            // Fetch EAV attributes for this block
            fetchEavAttributes(blockId).onComplete { ar ->
              if (ar.succeeded()) {
                val result = ar.result()
                val eavBool = result.eavAttributeBool
                val eavFloat = result.eavAttributeFloat
                val eavInt = result.eavAttributeInt
                val eavMoney = result.eavAttributeMoney
                val eavMultiSelect = result.eavAttributeMultiSelect
                val eavString = result.eavAttributeString
                val eavList = result.eavAttributeList

                val fullBlockInfo = FullBlockInfo(
                  blockId = blockIdObj,
                  backendBlock = backendBlock,
                  blockAttributes = blockAttributes,
                  eavAttributeBool = eavBool,
                  eavAttributeFloat = eavFloat,
                  eavAttributeInt = eavInt,
                  eavAttributeMoney = eavMoney,
                  eavAttributeMultiSelect = eavMultiSelect,
                  eavAttributeString = eavString,
                  eavAttributeList = eavList
                )

                fullBlockInfoList.add(fullBlockInfo)

                // If we've processed all blocks, send the reply
                if (fullBlockInfoList.size == blockInfoMap.size) {
                  message.reply(fullBlockInfoList, listDeliveryOptions)
                }
              } else {
                println("Failed to fetch EAV attributes: ${ar.cause()}")
                message.reply("Failed to fetch EAV attributes: ${ar.cause()}")
              }
            }
          }

          // If no blocks were found, send an empty list
          if (blockInfoMap.isEmpty()) {
            message.reply(fullBlockInfoList, listDeliveryOptions)
          }
        }
      }
    }
  }

  /**
   * Helper function to fetch all EAV attributes for a block
   */
  private fun fetchEavAttributes(blockId: Int): Future<FullEavAttribute> {
    val promise = Promise.promise<FullEavAttribute>()

    // Fetch boolean attributes
    val booleanQuery = "SELECT * FROM eav_attribute_bool WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val booleanFuture = client.preparedQuery(booleanQuery).execute(Tuple.of(blockId))

    // Fetch float attributes
    val floatQuery = "SELECT * FROM eav_attribute_float WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val floatFuture = client.preparedQuery(floatQuery).execute(Tuple.of(blockId))

    // Fetch int attributes
    val intQuery = "SELECT * FROM eav_attribute_int WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val intFuture = client.preparedQuery(intQuery).execute(Tuple.of(blockId))

    // Fetch money attributes
    val moneyQuery = "SELECT * FROM eav_attribute_money WHERE attribute_id IN (SELECT attribute_id FROM public.attribute_block WHERE block_id = ?)"
    val moneyFuture = client.preparedQuery(moneyQuery).execute(Tuple.of(blockId))

    // Fetch multi-select attributes
    val multiSelectQuery = "SELECT * FROM eav_attribute_multi_select WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val multiSelectFuture = client.preparedQuery(multiSelectQuery).execute(Tuple.of(blockId))

    // Fetch string attributes
    val stringQuery = "SELECT * FROM eav_attribute_string WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val stringFuture = client.preparedQuery(stringQuery).execute(Tuple.of(blockId))

    val listQuery = "SELECT * FROM attribute_list WHERE attribute_id IN (SELECT attribute_id FROM attribute_block WHERE block_id = ?)"
    val listFuture = client.preparedQuery(listQuery).execute(Tuple.of(blockId))

    // Compose all futures
    Future.all(booleanFuture, floatFuture, intFuture, moneyFuture, multiSelectFuture, stringFuture).onComplete { ar ->
      if (ar.succeeded()) {
        val booleanList = mutableListOf<EavAttributeBool>()
        booleanFuture.result().forEach { row -> booleanList.add(row.makeEavAttributeBool()) }

        val floatList = mutableListOf<EavAttributeFloat>()
        floatFuture.result().forEach { row -> floatList.add(row.makeEavAttributeFloat()) }

        val intList = mutableListOf<EavAttributeInt>()
        intFuture.result().forEach { row -> intList.add(row.makeEavAttributeInt()) }

        val moneyList = mutableListOf<EavAttributeMoney>()
        moneyFuture.result().forEach { row -> moneyList.add(row.makeEavAttributeMoney()) }

        val multiSelectList = mutableListOf<EavAttributeMultiSelect>()
        multiSelectFuture.result().forEach { row -> multiSelectList.add(row.makeEavAttributeMultiSelect()) }

        val stringList = mutableListOf<EavAttributeString>()
        stringFuture.result().forEach { row -> stringList.add(row.makeEavAttributeString()) }

        listFuture.onComplete {
          val listList = mutableListOf<EavAttributeList>()
          listFuture.result().forEach { row -> listList.add(row.makeAttributeList()) }

          promise.complete(
            FullEavAttribute(
            booleanList.toList(),
            floatList.toList(),
            intList.toList(),
            moneyList.toList(),
            multiSelectList.toList(),
            stringList.toList(),
            listList.toList())
          )
        }
      } else {
        promise.fail(ar.cause())
      }
    }

    return promise.future()
  }


  /**
   * Creates a BackendBlock object from a database row
   */
  private fun Row.makeBackendBlock(): BackendBlock {
    return BackendBlock(
      blockId = this.getInteger("block_id"),
      blockName = this.getString("block_name"),
      blockType = this.getString("block_type")
    )
  }

  /**
   * Creates a BlockAttribute object from a database row
   */
  private fun Row.makeBlockAttribute(): BlockAttribute {
    return BlockAttribute(
      attributeId = this.getString("attribute_id"),
      attributeName = this.getString("attribute_name"),
      attributeType = this.getString("attribute_type")
    )
  }

  /**
   * Creates an EavAttributeBool object from a database row
   */
  private fun Row.makeEavAttributeBool(): EavAttributeBool {
    return EavAttributeBool(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getBoolean("value")
    )
  }

  /**
   * Creates an EavAttributeFloat object from a database row
   */
  private fun Row.makeEavAttributeFloat(): EavAttributeFloat {
    return EavAttributeFloat(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getFloat("value")
    )
  }

  /**
   * Creates an EavAttributeInt object from a database row
   */
  private fun Row.makeEavAttributeInt(): EavAttributeInt {
    return EavAttributeInt(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getInteger("value")
    )
  }

  /**
   * Creates an EavAttributeMoney object from a database row
   */
  private fun Row.makeEavAttributeMoney(): EavAttributeMoney {
    return EavAttributeMoney(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getDouble("value")
    )
  }

  /**
   * Creates an EavAttributeMultiSelect object from a database row
   */
  private fun Row.makeEavAttributeMultiSelect(): EavAttributeMultiSelect {
    return EavAttributeMultiSelect(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getInteger("value")
    )
  }

  /**
   * Creates an EavAttributeString object from a database row
   */
  private fun Row.makeEavAttributeString(): EavAttributeString {
    return EavAttributeString(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("attribute_key"),
      value = this.getString("value")
    )
  }

  private fun Row.makeAttributeList(): EavAttributeList {
    return EavAttributeList(
      attributeId = this.getString("attribute_id"),
      attributeKey = this.getString("list_name")
    )
  }
}
