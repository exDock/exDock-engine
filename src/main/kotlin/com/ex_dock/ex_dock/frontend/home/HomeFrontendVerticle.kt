package com.ex_dock.ex_dock.frontend.home

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus

class HomeFrontendVerticle: AbstractVerticle() {

  private lateinit var eventBus: EventBus

  override fun start() {
    eventBus = vertx.eventBus()

    getHomePage()
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
