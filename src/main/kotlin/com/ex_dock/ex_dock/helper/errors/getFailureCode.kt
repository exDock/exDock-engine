package com.ex_dock.ex_dock.helper.errors

import io.vertx.core.eventbus.ReplyException

fun Throwable.failureCode(): Int {
  if (this is ReplyException) return this.failureCode()

  return 500
}
