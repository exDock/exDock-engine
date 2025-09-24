package com.ex_dock.ex_dock.helper.ctx

import com.ex_dock.ex_dock.helper.errors.failureCode
import io.vertx.ext.web.RoutingContext

fun RoutingContext.errorResponse(throwable: Throwable) {
  this.response().setStatusCode(throwable.failureCode()).end(throwable.message)
}
