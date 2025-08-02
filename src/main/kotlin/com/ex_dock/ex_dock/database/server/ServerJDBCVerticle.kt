package com.ex_dock.ex_dock.database.server

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class ServerJDBCVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val serverDataDataDeliveryOptions = DeliveryOptions().setCodecName("ServerDataDataCodec")
  private val serverVersionDataDeliveryOptions = DeliveryOptions().setCodecName("ServerVersionDataCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  companion object {
    private const val CACHE_ADDRESS_DATA = "server_data"
    private const val CACHE_ADDRESS_VERSION = "server_version"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections with the Server Data table
    getAllServerData()
    getServerDataByKey()
    createServerData()
    updateServerData()
    deleteServerData()

    // Initialize all eventbus connections with the Server Version table
    getAllServerVersionData()
    getServerVersionByKey()
    createServerVersion()
    updateServerVersion()
    deleteServerVersion()

    return Future.succeededFuture<Unit>()
  }

  private fun getAllServerData() {
    val getAllServerDataConsumer = eventBus.consumer<String>("process.server.getAllServerData")
    getAllServerDataConsumer.handler { message ->
      val query = JsonObject()

      client.find("server_data", query).replyListMessage(message)
    }
  }

  private fun getServerDataByKey() {
    val getServerDataByKeyConsumer = eventBus.consumer<String>("process.server.getServerDataByKey")
    getServerDataByKeyConsumer.handler { message ->
      val key = message.body()
      val query = JsonObject()
        .put("key", key)
      client.find("server_data", query).replySingleMessage(message)
    }
  }

  private fun createServerData() {
    val createServerDataConsumer = eventBus.consumer<ServerDataData>("process.server.createServerData")
    createServerDataConsumer.handler { message ->
      val serverData = message.body()
      val document = serverData.toDocument()

      val rowsFuture = client.save("server_data", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply(serverData, serverDataDataDeliveryOptions)
      }
    }
  }

  private fun updateServerData() {
    val updateServerDataConsumer = eventBus.consumer<ServerDataData>("process.server.updateServerData")
    updateServerDataConsumer.handler { message ->
      val body = message.body()
      val document = body.toDocument()
      val rowsFuture = client.save("server_data", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply(body, serverDataDataDeliveryOptions)
      }
    }
  }

  private fun deleteServerData() {
    val deleteServerDataConsumer = eventBus.consumer<String>("process.server.deleteServerData")
    deleteServerDataConsumer.handler{ message ->
      val key = message.body()
      val query = JsonObject()
        .put("key", key)
      val rowsFuture = client.removeDocuments("server_data", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
        }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply("Server data deleted successfully")
      }
    }
  }

  private fun getAllServerVersionData() {
    val getAllServerVersionDataConsumer = eventBus.consumer<String>("process.server.getAllServerVersionData")
    getAllServerVersionDataConsumer.handler { message ->
      val query = JsonObject()

      client.find("server_version", query).replyListMessage(message)
    }
  }

  private fun getServerVersionByKey() {
    val getServerVersionDataByKeyConsumer = eventBus.consumer<JsonObject>("process.server.getServerVersionDataByKey")
    getServerVersionDataByKeyConsumer.handler { message ->
      val key = message.body()
      val query = JsonObject()
        .put("major", key.getInteger("major"))
        .put("minor", key.getInteger("minor"))
        .put("patch", key.getInteger("patch"))
      val rowsFuture = client.find("server_version", query)

      rowsFuture.onFailure {
        message.fail(500, it.localizedMessage)
      }

      rowsFuture.onSuccess { res ->
        message.reply(res.first())
      }
    }
  }

  private fun createServerVersion() {
    val createServerVersionConsumer = eventBus.consumer<ServerVersionData>("process.server.createServerVersion")
    createServerVersionConsumer.handler { message ->
      val serverVersion = message.body()
      val document = serverVersion.toDocument()

      val rowsFuture = client.save("server_version", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply(serverVersion, serverVersionDataDeliveryOptions)
      }
    }
  }

  private fun updateServerVersion() {
    val updateServerVersionConsumer = eventBus.consumer<ServerVersionData>("process.server.updateServerVersion")
    updateServerVersionConsumer.handler { message ->
      val body = message.body()
      val document = body.toDocument()

      val rowsFuture = client.save("server_version", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply(body, serverVersionDataDeliveryOptions)
      }
    }
  }

  private fun deleteServerVersion() {
    val deleteServerVersionConsumer = eventBus.consumer<JsonObject>("process.server.deleteServerVersion")
    deleteServerVersionConsumer.handler { message ->
      val key = message.body()
      val query = JsonObject()
        .put("major", key.getInteger("major"))
        .put("minor", key.getInteger("minor"))
        .put("patch", key.getInteger("patch"))
      val rowsFuture = client.removeDocument("server_version", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
        }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply("Server version deleted successfully")
      }
    }
  }
}
