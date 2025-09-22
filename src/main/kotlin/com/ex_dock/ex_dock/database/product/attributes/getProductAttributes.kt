package com.ex_dock.ex_dock.database.product.attributes

import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient

fun EventBus.getProductAttributes(client: MongoClient) {
  this.localConsumer<JsonObject>("process.product.attributes.getAll").handler { message ->
    val body = message.body()
    val filter = JsonObject()
    val fieldsToReturn = JsonObject()
    val findOptions = FindOptions().setFields(fieldsToReturn)

    val fieldsToReturnInput: JsonArray? = body.getJsonArray("fieldsToReturn")

    if (fieldsToReturnInput != null) for (fieldToReturn in fieldsToReturnInput) {
      fieldsToReturn.put(fieldToReturn as String, 1)
    }

    client.findWithOptions("productAttributes", filter, findOptions).onFailure { err ->
      message.fail(500, err.message)
    }.onSuccess { res ->
      message.reply(res)
    }
  }
}
