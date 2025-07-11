package com.ex_dock.ex_dock.frontend.home

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus

class HomeFrontendVerticle: VerticleBase() {

  private lateinit var eventBus: EventBus

  override fun start(): Future<*>? {
    eventBus = vertx.eventBus()

    getHomePage()

    return Future.succeededFuture<Unit>()
  }

  private fun getHomePage() {
    val getHomeConsumer = eventBus.consumer<Any?>("frontend.retrieveHTML.home")
    getHomeConsumer.handler { message ->
      val data = getHomeData()
      eventBus.request<String>("template.generate.compiled", "home").onFailure {
        // TODO: implement
        println("eventbus request for 'template.generate.compiled' failed from within eventbus request 'frontend.retrieveHTML.home'")
      }.onSuccess { response ->
        message.reply(response.body())
      }
    }
  }

  private fun getHomeData(): Map<String, Any> {
    // TODO: get data needed for the home page
    return mapOf()
  }
}
