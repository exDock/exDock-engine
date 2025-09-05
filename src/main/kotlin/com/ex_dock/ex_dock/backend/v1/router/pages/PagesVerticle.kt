package com.ex_dock.ex_dock.backend.v1.router.pages

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.mongo.MongoClient

class PagesVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus


  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    return super.start()
  }
}
