package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.category.CategoryInfo
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.sales.*
import com.ex_dock.ex_dock.helper.AsyncExDockCache
import com.ex_dock.ex_dock.helper.CacheData
import com.ex_dock.ex_dock.helper.toVertxFuture
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.StringLoader
import io.pebbletemplates.pebble.template.PebbleTemplate
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import java.io.StringWriter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class TemplateEngineVerticle : VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private lateinit var templateCache: LoadingCache<String, TemplateCacheData>
  private lateinit var productCache: AsyncExDockCache<ProductInfo>
  private lateinit var categoryCache: AsyncExDockCache<CategoryInfo>
  private lateinit var creditMemoCache: AsyncExDockCache<CreditMemo>
  private lateinit var invoiceCache: AsyncExDockCache<Invoice>
  private lateinit var orderCache: AsyncExDockCache<Order>
  private lateinit var shipmentCache: AsyncExDockCache<Shipment>
  private lateinit var transactionCache: AsyncExDockCache<Transaction>
  private val engine = PebbleEngine.Builder().loader(StringLoader()).build()

  private val expireDuration = 10L
  private val refreshDuration = 1L
  private val maxHitCount = 100

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()
    productCache = AsyncExDockCache(vertx) { key -> getProductCacheData(key) }
    categoryCache = AsyncExDockCache(vertx) { key -> getCategoryCacheData(key) }
    creditMemoCache = AsyncExDockCache(vertx) { key -> getCreditMemoCacheData(key) }
    invoiceCache = AsyncExDockCache(vertx) { key -> getInvoiceCacheData(key) }
    orderCache = AsyncExDockCache(vertx) { key -> getOrderCacheData(key) }
    shipmentCache = AsyncExDockCache(vertx) { key -> getShipmentCacheData(key) }
    transactionCache = AsyncExDockCache(vertx) { key -> getTransactionCacheData(key) }

    templateCache = Caffeine.newBuilder()
      .expireAfterWrite(expireDuration, TimeUnit.MINUTES)
      .refreshAfterWrite(refreshDuration, TimeUnit.MINUTES)
      .build(CacheLoader { key ->
        val future = eventBus.request<JsonObject>("process.template.getTemplateByKey", key)
          .map { message ->
            TemplateCacheData(
              engine.getTemplate(message.body().getString("template_data")),
              0
            )
          }.otherwise { null }

        val javaFuture = future
          .toCompletionStage()
          .toCompletableFuture()

        return@CacheLoader try {
          javaFuture.join()
        } catch (e: Exception) {
          MainVerticle.logger.error { "Cache loader failed for key: $key\n${e.localizedMessage}" }
          null
        }
      })

    singleUseTemplate()
    getCompiledTemplate()
    invalidateCacheKey()

    return Future.succeededFuture<Unit>()
  }

  private fun singleUseTemplate() {
    eventBus.consumer("template.generate.singleUse") { message ->
      val body = message.body()

      val contextFuture: Future<MutableMap<String, Any?>> = getContextData(body).toVertxFuture()

      contextFuture.compose { context ->
        vertx.executeBlocking({
          try {
            val singleUseTemplateData: String = body.getString("templateData")
            val compiledTemplate = engine.getTemplate(singleUseTemplateData)
            val writer = StringWriter()

            compiledTemplate.evaluate(writer, context)
            return@executeBlocking writer.toString()
          } catch (e: Exception) {
            MainVerticle.logger.error { e.localizedMessage }
            throw e
          }
        }, false)
      }.onFailure { err ->
        message.fail(500, err.message)
      }.onSuccess { res ->
        message.reply(res)
      }
    }
  }

  private fun getCompiledTemplate() {
    eventBus.consumer("template.generate.compiled") { message ->
      val body = message.body()
      val contextFuture: Future<MutableMap<String, Any?>> = getContextData(body).toVertxFuture()

      contextFuture.compose { context ->
        vertx.executeBlocking({
          try {
            val key = body.getString("template_key")
            incrementTemplateHitCount(key)
            val template = templateCache.get(key)

            val writer = StringWriter()
            template.templateData.evaluate(writer, context)
            return@executeBlocking writer.toString()
          } catch (e: Exception) {
            MainVerticle.logger.error { e.localizedMessage }
            throw e
          }
        }, false)
      }.onFailure { err ->
        message.fail(500, err.message)
      }.onSuccess { res ->
        message.reply(res)
      }
    }
  }

  private fun incrementTemplateHitCount(key: String) {
    val templateCacheData = templateCache.getIfPresent(key)

    // Check if the cache data exists and is not expired or deleted
    if (templateCacheData != null) {

      // Check if the template data hits exceed the maximum hits or if the flag is set
      if (templateCacheData.hits >= maxHitCount) {
        templateCache.invalidate(key)
        println("CACHE DATA EXPIRED")
        return
      }

      templateCacheData.hits++
      templateCache.put(key, templateCacheData)
    }
  }

  private fun invalidateCacheKey() {
    eventBus.consumer<String>("template.cache.invalidate") { _ ->
      val keys = templateCache.asMap().keys

      for (key in keys) {
        templateCache.refresh(key)
        println("CACHE DATA REFRESHED FOR KEY: $key")
      }
    }
  }

  private fun getContextData(ids: JsonObject): CompletableFuture<MutableMap<String, Any?>> {
    val context: MutableMap<String, Any?> = mutableMapOf()
    val accessor = DataAccessor(
      productCache,
      categoryCache,
      creditMemoCache,
      invoiceCache,
      orderCache,
      shipmentCache,
      transactionCache
    )
    val futureMap: MutableMap<String, CompletableFuture<*>> = mutableMapOf()

    fun putFuture(key: String, type: String, idKey: String) {
      if (ids.containsKey(idKey)) {
        accessor.get(type, key = ids.getString(idKey))?.let { future ->
          futureMap[key] = future
        }
      }
    }

    putFuture("product", "product", "productId")
    putFuture("category", "category", "categoryId")
    putFuture("creditMemo", "credit_memo", "creditMemoId")
    putFuture("invoice", "invoice", "invoiceId")
    putFuture("order", "order", "orderId")
    putFuture("shipment", "shipment", "shipmentId")
    putFuture("transaction", "transaction", "transactionId")

    val futuresArray = futureMap.values.toTypedArray()
    return CompletableFuture.allOf(*futuresArray)
      .thenApply {
        val context: MutableMap<String, Any?> = mutableMapOf()

        futureMap.forEach { (key, future) ->
          val cacheData = future.join() as CacheData<*>?
          context[key] = cacheData?.data
        }

        context["accessor"] = accessor

        context
      }
      .exceptionally { e ->
        MainVerticle.logger.error(e) { "Failed to load all cache data for context" }
        emptyMap<String, Any?>().toMutableMap()
      }
  }

  private fun getProductCacheData(key: String): CompletableFuture<CacheData<ProductInfo>?> {
    return getCacheData(
      key = key,
      deserializer = ProductInfo.Companion::fromJson,
      eventBusAddress = "process.product.getProductById"
    )
  }

  private fun getCategoryCacheData(key: String): CompletableFuture<CacheData<CategoryInfo>?> {
    return getCacheData(
      key = key,
      deserializer = CategoryInfo.Companion::fromJson,
      eventBusAddress = "process.category.getCategoryById"
    )
  }

  private fun getCreditMemoCacheData(key: String): CompletableFuture<CacheData<CreditMemo>?> {
    return getCacheData(
      key = key,
      deserializer = CreditMemo.Companion::fromJson,
      eventBusAddress = "process.sales.getCreditMemoById"
    )
  }

  private fun getInvoiceCacheData(key: String): CompletableFuture<CacheData<Invoice>?> {
    return getCacheData(
      key = key,
      deserializer = Invoice.Companion::fromJson,
      eventBusAddress = "process.sales.getInvoiceById"
    )
  }

  private fun getOrderCacheData(key: String): CompletableFuture<CacheData<Order>?> {
    return getCacheData(
      key = key,
      deserializer = Order.Companion::fromJson,
      eventBusAddress = "process.sales.getOrderById"
    )
  }

  private fun getShipmentCacheData(key: String): CompletableFuture<CacheData<Shipment>?> {
    return getCacheData(
      key = key,
      deserializer = Shipment.Companion::fromJson,
      eventBusAddress = "process.sales.getShipmentById"
    )
  }

  private fun getTransactionCacheData(key: String): CompletableFuture<CacheData<Transaction>?> {
    return getCacheData(
      key = key,
      deserializer = Transaction.Companion::fromJson,
      eventBusAddress = "process.sales.getTransactionById"
    )
  }

  private fun <T : Any> getCacheData(
    key: String,
    deserializer: (JsonObject) -> T,
    eventBusAddress: String
  ): CompletableFuture<CacheData<T>?> {
    val future = eventBus.request<JsonObject>(eventBusAddress, key)
      .map { CacheData(deserializer(it.body()), 0) }
      .otherwise { null }

    return future.toCompletionStage().toCompletableFuture()
  }
}

private data class TemplateCacheData(
  var templateData: PebbleTemplate,
  var hits: Int,
)
