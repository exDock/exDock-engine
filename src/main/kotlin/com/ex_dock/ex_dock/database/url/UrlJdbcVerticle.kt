package com.ex_dock.ex_dock.database.url

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class UrlJdbcVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val urlKeysDeliveryOptions = DeliveryOptions().setCodecName("UrlKeysCodec")

  companion object {
    private const val CACHE_ADDRESS = "urls"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections with the url_keys table
    getAllUrlKeys()
    getUrlByKey()
    createUrlKey()
    updateUrlKey()
    deleteUrlKey()

    return Future.succeededFuture<Unit>()
  }

  private fun getAllUrlKeys() {
    val getAllUrlKeysConsumer = eventBus.consumer<String>("process.url.getAllUrlKeys")
    getAllUrlKeysConsumer.handler { message ->
      val query = JsonObject()
      client.find("url_keys", query).replyListMessage(message)
    }
  }

  private fun getUrlByKey() {
    val getUrlByKeyConsumer = eventBus.consumer<String>("process.url.getUrlByKey")
    getUrlByKeyConsumer.handler { message ->
      val urlKey = message.body()
      val query = JsonObject()
        .put("_id", urlKey)
      client.find("url_keys", query).replySingleMessage(message)
    }
  }

  private fun createUrlKey() {
    val createUrlKeyConsumer = eventBus.consumer<UrlKeys>("process.url.createUrlKey")
    createUrlKeyConsumer.handler { message ->
      val urlKey = message.body()
      val document = urlKey.toDocument()

      val rowsFuture = client.save("url_keys", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          urlKey.urlKey = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(urlKey, urlKeysDeliveryOptions)
      }
    }
  }

  private fun updateUrlKey() {
    val updateUrlKeyConsumer = eventBus.consumer<UrlKeys>("process.url.updateUrlKey")
    updateUrlKeyConsumer.handler { message ->
      val body = message.body()

      if (body.urlKey == null) {
        message.fail(400, failedMessage)
        return@handler
      }

      val document = body.toDocument()
      val rowsFuture = client.save("url_keys", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          body.urlKey = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, urlKeysDeliveryOptions)
      }
    }
  }

  private fun deleteUrlKey() {
    val deleteUrlKeyConsumer = eventBus.consumer<String>("process.url.deleteUrlKey")
    deleteUrlKeyConsumer.handler { message ->
      val urlKey = message.body()
      val query = JsonObject()
        .put("url_key", urlKey)
      val rowsFuture = client.removeDocument("url_keys", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("Url key deleted successfully")
      }
    }
  }
}
