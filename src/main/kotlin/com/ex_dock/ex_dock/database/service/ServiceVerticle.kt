package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.convertImage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class ServiceVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    imageConverter()

    return super.start()
  }

  private fun imageConverter() {
    eventBus.consumer<JsonObject>("process.service.convertImage") { message ->
      val body = message.body()
      val path = body.getString("path")
      var image = body.getString("body")
      image = image.substring(1, image.length - 1)
      println("Got request")
      convertImage(path, image)
      message.reply("Image conversion completed")
    }
  }
}
