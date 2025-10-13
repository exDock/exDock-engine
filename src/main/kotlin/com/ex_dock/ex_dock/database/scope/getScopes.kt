package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.global.cachedScopes
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient


internal fun EventBus.getAllScopes(client: MongoClient) {
  val getAllScopesConsumer = this.consumer<String>("process.scope.getAllScopes")
  getAllScopesConsumer.handler { message ->
    val query = JsonObject()

    client.find("scopes", query).onSuccess { res ->
      val newCachedScopes = JsonObject()
      for (scope in res) newCachedScopes.put(scope.getString("_id"), scope)
      cachedScopes = newCachedScopes
    }.replyListMessage(message)
  }
}

internal fun EventBus.getScopeById(client: MongoClient) {
  this.consumer<String>("process.scope.getScopeById").handler { message ->
    val websiteId = message.body()
    val query = JsonObject()
      .put("_id", websiteId)

    client.find("scopes", query).onSuccess { res ->
      cachedScopes.put(websiteId, res.first())
    }.replySingleMessage(message)
  }
}

internal fun EventBus.getScopesByWebsiteId(client: MongoClient) {
  val getScopesByWebsiteNameConsumer = this.consumer<String>("process.scope.getScopesByWebsiteId")
  getScopesByWebsiteNameConsumer.handler { message ->
    val websiteName = message.body()
    val query = JsonObject()
      .put("websiteId", websiteName)

    client.find("scopes", query).onSuccess { res ->
      for (scope in res) cachedScopes.put(scope.getString("_id"), scope)
    }.replyListMessage(message)
  }
}
