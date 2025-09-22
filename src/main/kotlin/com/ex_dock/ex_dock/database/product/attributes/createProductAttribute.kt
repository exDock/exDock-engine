package com.ex_dock.ex_dock.database.product.attributes

import com.ex_dock.ex_dock.database.product.attributes.types.ProductAttributeType
import com.ex_dock.ex_dock.database.product.attributes.types.ProductAttributeTypes
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

fun EventBus.createProductAttribute(client: MongoClient) {
  fun missingRequiredArgument(key: String): Nothing {
    throw IllegalArgumentException(
    "The required argument '$key' was not provided while trying to create a productAttribute"
    )
  }

  this.localConsumer<JsonObject>("process.product.attributes.create").handler { message ->
    val body = message.body()
    val productAttribute = JsonObject()

    val name: String = body.getString("name") ?: missingRequiredArgument("name")
    val key: String = body.getString("key") ?: missingRequiredArgument("key")
    val type: ProductAttributeType = ProductAttributeTypes.fromString(
      body.getString("type") ?: missingRequiredArgument("type")
    )
    val description: String? = body.getString("description")

    // TODO: check if key exists
  }
}
