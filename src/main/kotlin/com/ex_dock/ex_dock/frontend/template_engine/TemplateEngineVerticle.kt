package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.category.CategoryInfo
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.sales.CreditMemo
import com.ex_dock.ex_dock.database.sales.Invoice
import com.ex_dock.ex_dock.database.sales.Order
import com.ex_dock.ex_dock.database.sales.Shipment
import com.ex_dock.ex_dock.database.sales.Transaction
import com.ex_dock.ex_dock.helper.CacheData
import com.ex_dock.ex_dock.helper.ExDockCache
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
import java.util.concurrent.TimeUnit

class TemplateEngineVerticle : VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private lateinit var templateCache: LoadingCache<String, TemplateCacheData>
  private lateinit var productCache: ExDockCache<ProductInfo>
  private lateinit var categoryCache: ExDockCache<CategoryInfo>
  private lateinit var creditMemoCache: ExDockCache<CreditMemo>
  private lateinit var invoiceCache: ExDockCache<Invoice>
  private lateinit var orderCache: ExDockCache<Order>
  private lateinit var shipmentCache: ExDockCache<Shipment>
  private lateinit var transactionCache: ExDockCache<Transaction>
  private val engine = PebbleEngine.Builder().loader(StringLoader()).build()

  private val expireDuration = 10L
  private val refreshDuration = 1L
  private val maxHitCount = 100

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()
    productCache = ExDockCache(vertx) { key -> getProductCacheData(key) }
    categoryCache = ExDockCache(vertx) { key -> getCategoryCacheData(key) }
    creditMemoCache = ExDockCache(vertx) { key -> getCreditMemoCacheData(key) }
    invoiceCache = ExDockCache(vertx) { key -> getInvoiceCacheData(key) }
    orderCache = ExDockCache(vertx) { key -> getOrderCacheData(key) }
    shipmentCache = ExDockCache(vertx) { key -> getShipmentCacheData(key) }
    transactionCache = ExDockCache(vertx) { key -> getTransactionCacheData(key) }

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
    eventBus.consumer<JsonObject>("template.generate.singleUse") { message ->
      val body = message.body()
      vertx.executeBlocking({
        try {
          val singleUseTemplateData: String = body.getString("templateData")
          val compiledTemplate = engine.getTemplate(singleUseTemplateData)
          val writer = StringWriter()
          val context = getContextData(body)

          compiledTemplate.evaluate(writer, context)
          return@executeBlocking writer.toString()
        } catch (e: Exception) {
          MainVerticle.logger.error { e.localizedMessage }
          throw e
        }
      }, false).onFailure { err ->
        message.fail(500, err.message)
      }.onSuccess { res ->
        message.reply(res)
      }
    }
  }

  private fun getCompiledTemplate() {
    eventBus.consumer("template.generate.compiled") { message ->
      val body = message.body()
      val context = getContextData(body)

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
      }, false).onFailure { err ->
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

  private fun getContextData(ids: JsonObject): Map<String, Any?> {
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
    try {
      if (ids.containsKey("productId")) {
        context["product"] = accessor.get("product", key = ids.getString("productId"))
      }
      if (ids.containsKey("categoryId")) {
        context["category"] = accessor.get("category", key = ids.getString("categoryId"))
      }
      if (ids.containsKey("creditMemoId")) {
        context["creditMemo"] = accessor.get("credit_memo", key = ids.getString("creditMemoId"))
      }
      if (ids.containsKey("invoiceId")) {
        context["invoice"] = accessor.get("invoice", key = ids.getString("invoiceId"))
      }
      if (ids.containsKey("orderId")) {
        context["order"] = accessor.get("order", key = ids.getString("orderId"))
      }
      if (ids.containsKey("shipmentId")) {
        context["shipment"] = accessor.get("shipment", key = ids.getString("shipmentId"))
      }
      if (ids.containsKey("transactionId")) {
        context["transaction"] = accessor.get("transaction", key = ids.getString("transactionId"))
      }

      context["accessor"] = accessor
    } catch (e: Exception) {
      MainVerticle.logger.error { e.localizedMessage }
    }

    return context
  }

  private fun getProductCacheData(key: String): CacheData<ProductInfo>? {
    return getCacheData(
      key = key,
      deserializer = ProductInfo.Companion::fromJson,
      eventBusAddress = "process.product.getProductById"
    )
  }

  private fun getCategoryCacheData(key: String): CacheData<CategoryInfo>? {
    return getCacheData(
      key = key,
      deserializer = CategoryInfo.Companion::fromJson,
      eventBusAddress = "process.category.getCategoryById"
    )
  }

  private fun getCreditMemoCacheData(key: String): CacheData<CreditMemo>? {
    return getCacheData(
      key = key,
      deserializer = CreditMemo.Companion::fromJson,
      eventBusAddress = "process.sales.getCreditMemoById"
    )
  }

  private fun getInvoiceCacheData(key: String): CacheData<Invoice>? {
    return getCacheData(
      key = key,
      deserializer = Invoice.Companion::fromJson,
      eventBusAddress = "process.sales.getInvoiceById"
    )
  }

  private fun getOrderCacheData(key: String): CacheData<Order>? {
    return getCacheData(
      key = key,
      deserializer = Order.Companion::fromJson,
      eventBusAddress = "process.sales.getOrderById"
    )
  }

  private fun getShipmentCacheData(key: String): CacheData<Shipment>? {
    return getCacheData(
      key = key,
      deserializer = Shipment.Companion::fromJson,
      eventBusAddress = "process.sales.getShipmentById"
    )
  }

  private fun getTransactionCacheData(key: String): CacheData<Transaction>? {
    return getCacheData(
      key = key,
      deserializer = Transaction.Companion::fromJson,
      eventBusAddress = "process.sales.getTransactionById"
    )
  }

  private fun <T: Any> getCacheData(
    key: String,
    deserializer: (JsonObject) -> T,
    eventBusAddress: String
  ): CacheData<T>? {
    val future = eventBus.request<JsonObject>(eventBusAddress, key)
      .map { CacheData(deserializer(it.body()), 0)  }
      .otherwise { null }

    val javaFuture = future
      .toCompletionStage()
      .toCompletableFuture()

    return try {
      javaFuture.join()
    } catch (_: Exception) {
      MainVerticle.logger.error { "Cache loader failed for key: $key" }
      null
    }
  }
}

private data class TemplateCacheData(
  var templateData: PebbleTemplate,
  var hits: Int,
)
