package com.ex_dock.ex_dock.database.product.attributes

import com.ex_dock.ex_dock.helper.errors.failureCode
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun EventBus.getProductColumns() {
  this.localConsumer<String>("process.product.getColumns").handler { message ->
    this.request<JsonArray>(
      "process.product.attributes.getAll",
      JsonObject().put("fieldsToReturn", JsonArray(listOf("name", "key"))),
    ).onFailure { err ->
      message.fail(err.failureCode(), err.message)
    }.onSuccess { res ->
      message.reply(res.body())
    }
  }
}
