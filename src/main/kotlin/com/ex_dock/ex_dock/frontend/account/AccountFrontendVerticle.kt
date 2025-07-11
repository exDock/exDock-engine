package com.ex_dock.ex_dock.frontend.account

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus

class AccountFrontendVerticle: VerticleBase() {

  private lateinit var eventBus: EventBus

  override fun start(): Future<*> {
    return super.start()
  }
}
