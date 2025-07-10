package com.ex_dock.ex_dock.frontend.cache

import io.vertx.core.eventbus.EventBus

fun setCacheFlag(eventBus: EventBus, key: String) {
  eventBus.send("process.cache.invalidateKey", key)
  eventBus.send("template.cache.invalidate", "")
}
