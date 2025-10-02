package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import com.github.benmanes.caffeine.cache.AsyncCacheLoader
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.net.Address
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

open class AsyncExDockCache<T>(
  val cache: AsyncLoadingCache<String, CacheData<T>?>,
  val eventBus: EventBus,
  val maxHitCount: Int = 100
) {

  // Basic constructor that creates a cache with standard data
  constructor(vertx: Vertx, getCachedData: (String) -> CompletableFuture<CacheData<T>?>): this(
    cache = Caffeine.newBuilder()
      .maximumSize(10_000)
      .expireAfterWrite(1L, TimeUnit.HOURS)
      .refreshAfterWrite(1L, TimeUnit.HOURS)
      .executor { command -> Vertx.currentContext().runOnContext { _ -> command.run() } }
      .buildAsync(AsyncCacheLoader { key, _ -> getCachedData(key)}),
    eventBus = vertx.eventBus()
  )

  open fun getById(id: String): CompletableFuture<CacheData<T>?> {
    incrementHitCount(id)
    return cache.get(id)
  }

  open fun incrementHitCount(id: String) {
    val cacheData = cache.getIfPresent(id) ?: return

    cacheData.whenComplete { cacheData, throwable ->
      if (throwable != null) {
        return@whenComplete
      }

      val data = cacheData ?: return@whenComplete

      if (data.hits >= maxHitCount) {
        cache.synchronous().invalidate(id)
        return@whenComplete
      }

      data.hits = data.hits.plus(1)
      cache.put(id, CompletableFuture.completedFuture(data))
    }
  }
}

fun <T> CompletableFuture<T>.toVertxFuture(): Future<T> {
  return Future.fromCompletionStage(this)
}
