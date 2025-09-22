package com.ex_dock.ex_dock.database.scope

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

class ScopeJdbcVerticle:  VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val fullScopeDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("ScopeCodec")

  companion object {
    private const val CACHE_ADDRESS = "scopes"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for basic scopes
    getAllScopes()
    getScopeById()
    getScopesByWebsiteName()
    getScopesByStoreViewName()
    createScope()
    editScope()
    deleteScope()

    return Future.succeededFuture<Unit>()
  }

  private fun getAllScopes() {
    val getAllScopesConsumer = eventBus.consumer<String>("process.scope.getAllScopes")
    getAllScopesConsumer.handler { message ->
      val query = JsonObject()

      client.find("scopes", query).replyListMessage(message)
    }
  }

  private fun getScopeById() {
    val getScopeByWebsiteIdConsumer = eventBus.consumer<String>("process.scope.getScopeByWebsiteId")
    getScopeByWebsiteIdConsumer.handler { message ->
      val websiteId = message.body()
      val query = JsonObject()
        .put("_id", websiteId)

      client.find("scopes", query).replySingleMessage(message)
    }
  }

  private fun getScopesByWebsiteName() {
    val getScopesByWebsiteNameConsumer = eventBus.consumer<String>("process.scope.getScopesByWebsiteName")
    getScopesByWebsiteNameConsumer.handler { message ->
      val websiteName = message.body()
      val query = JsonObject()
        .put("website_name", websiteName)

      client.find("scopes", query).replyListMessage(message)
    }
  }

  private fun getScopesByStoreViewName() {
    val getScopesByStoreViewNameConsumer = eventBus.consumer<String>("process.scope.getScopesByStoreViewName")
    getScopesByStoreViewNameConsumer.handler { message ->
      val storeViewName = message.body()
      val query = JsonObject()
        .put("store_view_name", storeViewName)

      client.find("scopes", query).replyListMessage(message)
    }
  }

  private fun createScope() {
    val createScopeConsumer = eventBus.consumer<Scope>("process.scope.createScope")
    createScopeConsumer.handler { message ->
      println("Received createScope message in ScopeJdbcVerticle")
      val scope = message.body()
      val document = scope.toDocument()

      val rowsFuture = client.save("scopes", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          scope.scopeId = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(scope, fullScopeDeliveryOptions)
      }
    }
  }

  private fun editScope() {
    val editScopeConsumer = eventBus.consumer<Scope>("process.scope.editScope")
    editScopeConsumer.handler { message ->
      val body = message.body()
      if (body.scopeId == null) {
        message.fail(400, "No scope ID provided")
        return@handler
      }
      val document = body.toDocument()
      val rowsFuture = client.save("scopes", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          body.scopeId = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, fullScopeDeliveryOptions)
      }
    }
  }

  private fun deleteScope() {
    val deleteScopeConsumer = eventBus.consumer<String>("process.scope.deleteScope")
    deleteScopeConsumer.handler { message ->
      val scopeId = message.body()
      val query = JsonObject()
        .put("_id", scopeId)

      val rowsFuture = client.removeDocument("scopes", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        message.reply("Scope deleted successfully")
      }
    }
  }
}
