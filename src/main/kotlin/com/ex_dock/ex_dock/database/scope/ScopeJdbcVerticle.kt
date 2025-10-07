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
    eventBus.getAllScopes(client)
    eventBus.getScopeById(client)
    eventBus.getScopesByWebsiteName(client)
    eventBus.getScopesByStoreViewName(client)
    eventBus.createWebsite(client)
    eventBus.createStoreView(client)
    editScope()
    deleteScope()

    return Future.succeededFuture<Unit>()
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
        message.fail(500, "Failed to execute query: $res")
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
        message.fail(500, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        message.reply("Scope deleted successfully")
      }
    }
  }
}
