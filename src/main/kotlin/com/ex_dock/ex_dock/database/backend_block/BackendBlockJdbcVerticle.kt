package com.ex_dock.ex_dock.database.backend_block

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class BackendBlockJdbcVerticle : VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val backendBlockDeliveryOptions = DeliveryOptions().setCodecName("BlockInfoCodec")

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for backend blocks
    getAllBackendBlocks()
    getBackendBlockById()
    getBackendBlocksByPageName()
    createBackendBlock()
    editBackendBlock()
    deleteBackendBlock()

    return Future.succeededFuture<Unit>()
  }

  fun getAllBackendBlocks() {
    val getAllBackendBlocksConsumer = eventBus.consumer<String>("process.backendBlock.getAllBackendBlocks")
    getAllBackendBlocksConsumer.handler { message ->
      val query = JsonObject()
      client.find("backend_blocks", query).replyListMessage(message)
    }
  }

  fun getBackendBlockById() {
    val getBackendBlockByIdConsumer = eventBus.consumer<String>("process.backendBlock.getBackendBlockById")
    getBackendBlockByIdConsumer.handler { message ->
      val backendBlockId = message.body()
      val query = JsonObject()
        .put("_id", backendBlockId)
      client.find("backend_blocks", query).replySingleMessage(message)
    }
  }

  fun getBackendBlocksByPageName() {
    val getBackendBlocksByPageNameConsumer =
      eventBus.consumer<String>("process.backendBlock.getBackendBlocksByPageName")
    getBackendBlocksByPageNameConsumer.handler { message ->
      val pageName = message.body()
      val query = JsonObject()
        .put("page_name", pageName)
      client.find("backend_blocks", query).replyListMessage(message)
    }
  }

  fun createBackendBlock() {
    val createBackendBlockConsumer = eventBus.consumer<BlockInfo>("process.backendBlock.createBackendBlock")
    createBackendBlockConsumer.handler { message ->
      val backendBlock = message.body()
      val document = backendBlock.toDocument()

      val rowsFuture = client.save("backend_blocks", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        backendBlock.blockId = lastInsertID

        message.reply(backendBlock, backendBlockDeliveryOptions)
      }
    }
  }

  fun editBackendBlock() {
    val editBackendBlockConsumer = eventBus.consumer<BlockInfo>("process.backendBlock.editBackendBlock")
    editBackendBlockConsumer.handler { message ->
      val body = message.body()

      if (body.blockId == null) {
        message.fail(400, "No block ID provided")
        return@handler
      }
      val document = body.toDocument()
      val rowsFuture = client.save("backend_blocks", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        body.blockId = lastInsertID

        message.reply(body, backendBlockDeliveryOptions)
      }
    }
  }

  fun deleteBackendBlock() {
    val deleteBackendBlockConsumer = eventBus.consumer<String>("process.backendBlock.deleteBackendBlock")
    deleteBackendBlockConsumer.handler { message ->
      val backendBlockId = message.body()
      val query = JsonObject()
        .put("_id", backendBlockId)

      val rowsFuture = client.removeDocument("backend_blocks", query)
      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
        }

      rowsFuture.onSuccess { res ->
        message.reply("Backend Block deleted successfully")
      }
    }
  }

}
