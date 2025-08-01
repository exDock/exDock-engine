package com.ex_dock.ex_dock.database.connection

import com.ex_dock.ex_dock.helper.load
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import java.util.*


fun Vertx.getConnection(): MongoClient {
  var client: MongoClient
  val connectOptions = JsonObject()

  try {
    val props = Properties().load()
    val p = Properties()
    p.setProperty("database", "ex-dock")

    connectOptions
      .put("connection_string", props.getProperty("DATABASE_STRING"))
      .put("db_name", "ex-dock")

    client = MongoClient.createShared(this, connectOptions)
    return client

  } catch (_: Exception) {
    try {
      val isDocker: Boolean = !System.getenv("GITHUB_RUN_NUMBER").isNullOrEmpty()
      if (isDocker) {
        val p = Properties()
        p.setProperty("database", "ex-dock")

        connectOptions
          .put("connection_string", "mongodb://admin:docker@localhost:8890/")
          .put("db_name", "ex-dock")

        client = MongoClient.createShared(this, connectOptions)
        return client
      } else {
        error("Could not load the Properties file!")
      }
    } catch (_: Exception) {
      error("Could not read the Properties file!")
    }
  }

  client = MongoClient.create(this, connectOptions)
  return client
}
