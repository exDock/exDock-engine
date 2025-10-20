package com.ex_dock.ex_dock.helper.futures

import io.vertx.core.Future
import io.vertx.core.Promise

fun <T> Future<T>.onFailure(promise: Promise<*>): Future<T> {
  return this.onFailure { err ->
    promise.fail(err)
  }
}
