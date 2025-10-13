package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import com.ex_dock.ex_dock.helper.codecs.deliveryOptions
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class ScopeJdbcVerticle:  VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val fullScopeDeliveryOptions: DeliveryOptions = Scope::class.deliveryOptions()

  companion object {
    const val CACHE_ADDRESS = "scopes"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for basic scopes
    eventBus.getAllScopes(client)
    eventBus.getScopeById(client)
    eventBus.getScopesByWebsiteId(client)
    eventBus.createWebsite(client)
    eventBus.createStoreView(client)
    deleteScope()

    return Future.succeededFuture<Unit>()
  }

  // TODO: create editScope functions

  private fun deleteScope() {
    // TODO: remove all data associated with the scope
    val deleteScopeConsumer = eventBus.consumer<String>("process.scope.deleteScope")
    deleteScopeConsumer.handler { message ->
      val scopeId = message.body()
      val query = JsonObject()
        .put("_id", scopeId)

      val rowsFuture = client.removeDocument("scopes", query)

      rowsFuture.onFailure { err ->
        println("Failed to execute query: $err")
        message.fail(500, "Failed to execute query: $err")
      }

      rowsFuture.onSuccess { _ ->
        message.reply("Scope deleted successfully")
      }
    }
  }
}
