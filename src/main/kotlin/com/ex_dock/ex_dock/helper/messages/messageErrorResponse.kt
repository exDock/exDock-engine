package com.ex_dock.ex_dock.helper.messages

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.errors.failureCode
import io.vertx.core.eventbus.Message

fun Message<*>.errorResponse(throwable: Throwable) {
  this.errorResponse(throwable.failureCode(), throwable)
}

fun Message<*>.errorResponse(failureCode: Int, throwable: Throwable) {
  this.errorResponse(failureCode, throwable.message ?: "Missing throwable error message")
}

fun Message<*>.errorResponse(failureCode: Int, message: String) {
  MainVerticle.logger.error { "message.errorResponse() [${failureCode}] $message" }
  this.fail(failureCode, message)
}
