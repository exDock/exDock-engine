package com.ex_dock.ex_dock.websocket

import io.vertx.core.Future
import io.vertx.core.VerticleBase

class WebsocketVerticle: VerticleBase() {
  override fun start(): Future<*>? {
    return Future.succeededFuture<Unit>()
  }
}
