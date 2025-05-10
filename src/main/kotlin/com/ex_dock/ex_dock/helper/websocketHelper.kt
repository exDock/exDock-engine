package com.ex_dock.ex_dock.helper

import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import java.time.Instant
import java.time.format.DateTimeFormatter

fun EventBus.sendError(error: Exception, targetType: String = "BROADCAST", targetIdentifier: String = "") {
  val errorObject = JsonObject()
    .put("errorType", error::class.simpleName)
    .put("errorMessage", error.message)
    .put("timeStamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))

  this.send("process.websocket.broadcastError", JsonObject()
    .put("errorPayload", errorObject)
    .put("targetType", targetType)
    .put("targetIdentifier", targetIdentifier)
  )
}
