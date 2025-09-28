package com.ex_dock.ex_dock.database.product.attributes

import com.ex_dock.ex_dock.MainVerticle
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

    client.find("productAttributes", JsonObject().put("key", key)).onFailure { err ->
      message.fail(500, err.message)
    }.onSuccess { res ->
      if (res.isNotEmpty()) {
        MainVerticle.logger.error { "Cannot create product attribute: Product attribute with key '$key' already exists" }
        return@onSuccess message.fail(409, "Product attribute with key '$key' already exists")
      }

      productAttribute.put("name", name)
      productAttribute.put("key", key)
      productAttribute.put("type", type.toString())
      if (description != null) productAttribute.put("description", description)

      client.save("productAttributes", productAttribute).onFailure { err ->
        message.fail(500, err.message)
      }.onSuccess { res ->
        message.reply(res)  // Returns the id that was created
      }
    }
  }
}
