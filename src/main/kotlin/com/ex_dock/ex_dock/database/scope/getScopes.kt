package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient


internal fun EventBus.getAllScopes(client: MongoClient) {
  val getAllScopesConsumer = this.consumer<String>("process.scope.getAllScopes")
  getAllScopesConsumer.handler { message ->
    val query = JsonObject()

    client.find("scopes", query).replyListMessage(message)
  }
}

internal fun EventBus.getScopeById(client: MongoClient) {
  val getScopeByWebsiteIdConsumer = this.consumer<String>("process.scope.getScopeByWebsiteId")
  getScopeByWebsiteIdConsumer.handler { message ->
    val websiteId = message.body()
    val query = JsonObject()
      .put("_id", websiteId)

    client.find("scopes", query).replySingleMessage(message)
  }
}

internal fun EventBus.getScopesByWebsiteName(client: MongoClient) {
  val getScopesByWebsiteNameConsumer = this.consumer<String>("process.scope.getScopesByWebsiteName")
  getScopesByWebsiteNameConsumer.handler { message ->
    val websiteName = message.body()
    val query = JsonObject()
      .put("website_name", websiteName)

    client.find("scopes", query).replyListMessage(message)
  }
}

internal fun EventBus.getScopesByStoreViewName(client: MongoClient) {
  val getScopesByStoreViewNameConsumer = this.consumer<String>("process.scope.getScopesByStoreViewName")
  getScopesByStoreViewNameConsumer.handler { message ->
    val storeViewName = message.body()
    val query = JsonObject()
      .put("store_view_name", storeViewName)

    client.find("scopes", query).replyListMessage(message)
  }
}
