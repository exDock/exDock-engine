package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.net.Address
import java.util.concurrent.TimeUnit

open class ExDockCache<T>(
  val cache: LoadingCache<String, CacheData<T>>,
  val eventBus: EventBus,
  val maxHitCount: Int = 100
) {

  // Basic constructor that creates a cache with standard data
  constructor(vertx: Vertx, getCachedData: (String) -> CacheData<T>?): this(
    cache = Caffeine.newBuilder()
      .maximumSize(10_000)
      .expireAfterWrite(1L, TimeUnit.HOURS)
      .refreshAfterWrite(1L, TimeUnit.HOURS)
      .build<String, CacheData<T>>(CacheLoader { key -> getCachedData(key)}),
    eventBus = vertx.eventBus()
  )

  open fun getById(id: String): T {
    incrementHitCount(id)
    return cache.get(id).data
  }

  open fun incrementHitCount(id: String) {
    val cacheData = cache.getIfPresent(id)

    // Check if the cache data exists and is not expired or deleted
    if (cacheData != null) {
      // Check if the template data hits exceed the maximum hits or if the flag is set
      if (cacheData.hits >= maxHitCount) {
        cache.invalidate(id)
        println("CACHE DATA EXPIRED")
        return
      }

      cacheData.hits = cacheData.hits.plus(1)
      cache.put(id, cacheData)
    }
  }
}

data class CacheData<T>(
  var data: T,
  var hits: Int,
)
