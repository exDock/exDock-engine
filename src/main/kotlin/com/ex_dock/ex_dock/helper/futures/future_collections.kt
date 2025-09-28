package com.ex_dock.ex_dock.helper.futures

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise

inline fun <reified T> MutableList<Future<T>>.addFuture(handler: Handler<Promise<T>>) {
  this.add(Future.future<T>(handler))
}
