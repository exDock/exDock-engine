package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

val listDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

fun Future<List<JsonObject>>.replyListMessage(message: Message<String>) {
  this.onFailure {
    MainVerticle.logger.error { it.localizedMessage }
    message.fail(500, it.localizedMessage)
  }

  this.onSuccess { res ->
    message.reply(res, listDeliveryOptions)
  }
}

fun Future<List<JsonObject>>.replySingleMessage(message: Message<String>) {
  this.onFailure {
    MainVerticle.logger.error { it.localizedMessage }
    message.fail(500, it.localizedMessage)
  }

  this.onSuccess { res ->
    if (res.isEmpty()) {
      message.fail(404, "Not found")
      return@onSuccess
    }
    message.reply(res.first())
  }
}
