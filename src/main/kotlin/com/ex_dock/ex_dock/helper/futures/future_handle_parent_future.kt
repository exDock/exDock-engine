package com.ex_dock.ex_dock.helper.futures

import io.vertx.core.Future
import io.vertx.core.Promise

/**
 * This onFailure method replaces the standard onFailure { promise.fail(it) }
 */
fun <T> Future<T>.onFailure(promise: Promise<*>): Future<T> {
  return this.onFailure { err ->
    promise.fail(err)
  }
}

/**
 * This onSuccess method replaces the standard onSuccess { promise.complete(it) }
 */
fun <T> Future<T>.onSuccess(promise: Promise<T>): Future<T> {
  return this.onSuccess { res ->
    promise.complete(res)
  }
}

/**
 * This onSuccess method replaces the standard onSuccess { promise.complete() }
 */
fun <T> Future<T>.onSuccess(promise: Promise<Unit>): Future<T> {
  return this.onSuccess { _ ->
    promise.complete()
  }
}
