package com.ex_dock.ex_dock.database.connection

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.load
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import java.util.*


fun Vertx.getConnection(): MongoClient {
  var client: MongoClient
  val connectOptions = JsonObject()

  try {
    val isDocker: Boolean = !System.getenv("GITHUB_RUN_NUMBER").isNullOrEmpty()
    if (isDocker) {
      MainVerticle.logger.info { "Running inside GitHub Docker container" }
      val p = Properties().load()
      p.setProperty("database", "ex-dock")

      connectOptions
        .put("connection_string", "mongodb://admin:docker@localhost:8890/")
        .put("db_name", "ex-dock")

      client = MongoClient.createShared(this, connectOptions)
      return client
    }
    val props = Properties().load()
    val p = Properties()
    p.setProperty("database", "ex-dock")

    connectOptions
      .put("connection_string", props.getProperty("DATABASE_STRING"))
      .put("db_name", "ex-dock")

    client = MongoClient.createShared(this, connectOptions)
    return client

  } catch (_: Exception) {
    error("Could not read the Properties file!")
  }
}
