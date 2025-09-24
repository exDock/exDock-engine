package com.ex_dock.ex_dock.frontend.cache

import com.ex_dock.ex_dock.database.service.InvalidCacheKeyException
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.TimeUnit

class CacheVerticle : VerticleBase() {
  private lateinit var cache: LoadingCache<String, CacheData>
  private val mapDeliveryOptions = DeliveryOptions().setCodecName("MapCodec")
  private val expireDuration = 10L
  private val refreshDuration = 1L
  private val maxHitCount = 100

  private lateinit var eventBus: EventBus

  override fun start(): Future<*>? {
    eventBus = vertx.eventBus()

    // Initialize the cache with a CacheLoader that handles cache population and refresh
    cache = Caffeine.newBuilder()
      .expireAfterWrite(expireDuration, TimeUnit.MINUTES)
      .refreshAfterWrite(refreshDuration, TimeUnit.MINUTES)
      .build { key -> loadCacheData(key) }

    // Initialize the eventbus address to handle the cache requests
    getData()
    cacheInvalidateHandler()

    return Future.succeededFuture<Unit>()
  }

  private fun getData() {
    val cacheConsumer = eventBus.consumer<String>("process.cache.requestData")
    cacheConsumer.handler { message ->
      val futures = mutableListOf<Future<Unit>>()
      val requestedData = message.body()
      val cacheMap = emptyMap<String, List<Any>>().toMutableMap()
      val keyString = requestedData.split(";")

      // Iterate through all keys in the key string
      for (key in keyString) {
        futures.add(Future.future { promise ->
          var cacheData = cache.getIfPresent(key)

          if (cacheData == null) {
            cacheData = CacheData(
              Future.succeededFuture(listOf("No data Found!")),
              0,
            )
          }

          cacheData.data.onFailure { err ->
            promise.fail(err.message)
          }.onSuccess { res ->
            incrementHitCount(key)
            cacheMap[key] = res
            promise.complete()
          }
        })
      }

      // Wait for all cache keys to be processed before replying
      Future.all<Unit>(futures).onComplete { ar ->
        if (ar.succeeded()) {
          // Once all the futures are completed, send the response back
          message.reply(cacheMap.toMap(), mapDeliveryOptions)
        } else {
          // Handle any failure (shouldn't occur in this case)
          message.fail(500, ar.cause().message)
        }
      }
    }
  }

  /**
   * Uses CacheLoader to load the cache data and avoids recursion or invalidation
   * @param key The cache key
   * @return CacheData that represents the loaded cache data
   */
  private fun loadCacheData(key: String): CacheData {
    return when (key) {
      "accounts" -> getDataFromDatabase("account.getAllFullUserInfo")
      "categories" -> getDataFromDatabase("categories.getAllFullInfo")
      "custom_attributes" -> getDataFromDatabase("attributes.getAllCustomAttributes")
      "global_eav" -> getDataFromDatabase("eavGlobal.getAllEavGlobalInfo")
      "products" -> getDataFromDatabase("products.getAllFullProducts")
      "multi_select" -> getDataFromDatabase("multiSelect.getAllMultiSelectAttributesInfo")
      "store_view_eav" -> getDataFromDatabase("eavStoreView.getAllEavStoreViewInfo")
      "website_eav" -> getDataFromDatabase("eavWebsite.getAllEavWebsiteInfo")
      "scopes" -> getDataFromDatabase("scope.getAllScopes")
      "server_data" -> getDataFromDatabase("server.getAllServerData")
      "server_version" -> getDataFromDatabase("server.getAllServerVersions")
      "text_pages" -> getDataFromDatabase("textPages.getAllFullTextPages")
      "urls" -> getDataFromDatabase("url.getAllFullUrls")
      else -> throw InvalidCacheKeyException("Unknown cache key: $key")
    }
  }

  /**
   * Increments the hit count of the data with the given key and invalidates
   * the cache when the hit count exceeds the maximum hit count
   */
  private fun incrementHitCount(key: String) {
    val cacheData = cache.getIfPresent(key)

    // Check if the cache data exists and is not expired or deleted
    if (cacheData != null) {
      // If hits exceed the threshold, reset the cache
      if (cacheData.hits >= maxHitCount) {
        // We invalidate the cache directly, this is safe because we use CacheLoader
        cache.invalidate(key)
        println("CACHE DATA EXPIRED")
        return
      }

      // Increment the hit count
      cacheData.hits++
      cache.put(key, cacheData)  // Refresh the cache after incrementing the hit count
    }
  }

  private fun getDataFromDatabase(address: String): CacheData {
    // Initialize a new CacheData object to avoid null values
    val cacheData = CacheData(
      data = Future.future {},
      hits = 0,
    )

    // Fetch data asynchronously
    cacheData.data = Future.future { promise ->
      eventBus.request<MutableList<Any>>("process.$address", "").onFailure {
        promise.fail("Failed to load cache data")
      }.onSuccess { res ->
        promise.complete(res.body().toList())
      }
    }

    return cacheData
  }

  /**
   * Sets a specified cache data flag
   */
  private fun cacheInvalidateHandler() {
    eventBus.consumer("process.cache.invalidateKey") { message ->
      val cacheKey = message.body()
      val cacheData = cache.getIfPresent(cacheKey)

      // Set the cache data flag if the cache entry exists
      if (cacheData != null) {
        cache.refresh(cacheKey)
        message.reply("Cache flag was successfully set")
        return@consumer
      }

      message.reply("")
    }
  }
}

/**
 * Cache data object for use in the cache
 * @param data The data from the database as a list
 * @param hits The number of times the cache was accessed
 */
data class CacheData(
  var data: Future<List<Any>>,
  var hits: Int,
)


