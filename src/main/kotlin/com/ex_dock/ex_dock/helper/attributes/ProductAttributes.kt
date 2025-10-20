package com.ex_dock.ex_dock.helper.attributes

import io.vertx.ext.mongo.MongoClient
import kotlin.reflect.KClass

class ProductAttributes(client: MongoClient) : Attributes(client) {
  override val collection: String = "products"
  override val allowedTypes: Map<String, KClass<*>> = mapOf(
    "string" to String::class,
    "integer" to Int::class,
    "number" to Number::class,
    "boolean" to Boolean::class,
  )
}
