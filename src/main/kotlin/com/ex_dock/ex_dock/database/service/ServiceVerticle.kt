package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.convertImage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.toDocument
import com.ex_dock.ex_dock.helper.load
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.UpdateOptions
import java.util.Properties
import org.bson.types.ObjectId

class ServiceVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    imageConverter()
    addAdminUser()

    return super.start()
  }

  private fun addAdminUser() {
    val props = Properties().load()
    val adminEmail = props.getProperty("ADMIN_EMAIL")
    val adminPassword = props.getProperty("ADMIN_PASSWORD")
    val adminUser = FullUser(
      userId = ObjectId().toHexString(),
      email = adminEmail,
      password = adminPassword,
      permissions = listOf(),
      apiKey = "test"
    )
    val query = JsonObject().put("email", adminUser.email)
    val update = JsonObject().put($$"$setOnInsert", adminUser.toDocument())
    val findOptions = FindOptions()
    val options = UpdateOptions().setUpsert(true)
    client.findOneAndUpdateWithOptions("users", query, update, findOptions, options).onSuccess {
      MainVerticle.logger.info { "Admin user created" }
    }.onFailure {
      MainVerticle.logger.error { "Failed to create admin user" }
    }
  }

  private fun imageConverter() {
    eventBus.consumer<JsonObject>("process.service.convertImage") { message ->
      val body = message.body()
      val path = body.getString("path")
      var image = body.getString("body")
      image = image.substring(1, image.length - 1)
      println("Got request")
      eventBus.convertImage(path, image).onFailure { err ->
        MainVerticle.logger.error { err.localizedMessage }
        message.fail(500, err.localizedMessage)
      }.onSuccess {
        MainVerticle.logger.info { "Image conversion completed" }
        message.reply("Image conversion completed")
      }
    }
  }
}
