package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.helper.messages.errorResponse
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

internal fun EventBus.createWebsite(client: MongoClient) {
  this.localConsumer<JsonObject>("process.scope.create.website").handler { message ->
    val data = message.body()

    val key = data.getString("scopeKey")
      ?: return@handler message.fail(400, "The key of the website (scope) is required.")
    val name = data.getString("scopeName")
      ?: return@handler message.fail(400, "The name of the website (scope) is required.")

    client.findOne(
      ScopeJdbcVerticle.CACHE_ADDRESS,
      JsonObject().put("_id", key),
      JsonObject().put("_id", 1),
    ).onFailure { err ->
      message.errorResponse(400, err)
    }.onSuccess { res ->
      if (res != null)
        return@onSuccess message.fail(409, "This function is for creating websites (scope), not editing them.")

      val document = JsonObject()
        .put("_id", key)
        .put("scopeName", name)
        .put("scopeType", "website")

      client.insert(ScopeJdbcVerticle.CACHE_ADDRESS, document).onFailure { err ->
        message.errorResponse(err)
      }.onSuccess { res ->
        message.reply(res)
      }
    }
  }
}

internal fun EventBus.createStoreView(client: MongoClient) {
  this.localConsumer<JsonObject>("process.scope.create.store-view").handler { message ->
    val data = message.body()

    val name = data.getString("name")
      ?: return@handler message.fail(400, "The name of the store-view (scope) is required.")
    val key = data.getString("key")
      ?: return@handler message.fail(400, "The key of the store-view (scope) is required.")
    val websiteId = data.getString("websiteId")
      ?: return@handler message.fail(400, "The websiteId of the parent website (scope) is required.")

    client.findOne(
      ScopeJdbcVerticle.CACHE_ADDRESS,
      JsonObject().put("_id", key),
      JsonObject().put("_id", 1),
    ).onFailure { err ->
      message.errorResponse(400, err)
    }.onSuccess { res ->
      if (res != null)
        return@onSuccess message.fail(409, "This function is for creating store-views (scope), not editing them.")

      val searchWebsiteQuery = JsonObject().put("scopeType", "website").put("_id", websiteId)
      client.find(ScopeJdbcVerticle.CACHE_ADDRESS, searchWebsiteQuery).onFailure { err ->
        message.errorResponse(err)
      }.onSuccess { res ->
        if (res.isEmpty())
          return@onSuccess message.fail(400, "The websiteId of the parent website (scope) does not exist")

        client.find(ScopeJdbcVerticle.CACHE_ADDRESS, JsonObject().put("_id", key)).onFailure { err ->
          message.errorResponse(err)
        }.onSuccess { res ->
          if (res.isNotEmpty())
            return@onSuccess message.fail(400, "The key of the store-view (scope) already exists for a scope")

          val document = JsonObject()
            .put("_id", key)
            .put("scopeName", name)
            .put("scopeType", "store-view")
            .put("websiteId", websiteId)

          client.insert(ScopeJdbcVerticle.CACHE_ADDRESS, document).onFailure { err ->
            message.errorResponse(err)
          }.onSuccess { res ->
            message.reply(res)
          }
        }
      }
    }
  }
}
