package com.ex_dock.ex_dock.helper.ctx

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

fun RoutingContext.jsonResponse(jsonObject: JsonObject) {
  this.response().end(jsonObject.encode())
}


fun RoutingContext.jsonResponse(jsonObjectMessage: Message<JsonObject>) {
  this.jsonResponse(jsonObjectMessage.body())
}
