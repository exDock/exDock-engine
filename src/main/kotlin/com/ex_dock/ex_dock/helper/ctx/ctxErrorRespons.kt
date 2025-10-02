package com.ex_dock.ex_dock.helper.ctx

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.errors.failureCode
import io.vertx.ext.web.RoutingContext

fun RoutingContext.errorResponse(throwable: Throwable) {
  this.errorResponse(throwable.failureCode(), throwable)
}

fun RoutingContext.errorResponse(failureCode: Int, throwable: Throwable) {
  this.errorResponse(failureCode, throwable.message ?: "Missing throwable error message")
}

fun RoutingContext.errorResponse(failureCode: Int, message: String) {
  MainVerticle.logger.error { "ctx.errorResponse() [${failureCode}] $message" }
  this.response().setStatusCode(failureCode).end(message)
}
