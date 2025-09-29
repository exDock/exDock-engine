package com.ex_dock.ex_dock.database.url

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class UrlJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val urlKeysDeliveryOptions = DeliveryOptions().setCodecName("UrlKeysCodec")
  private var testUrlKey = UrlKeys(
    urlKey = "testUrl",
    upperKey = "testUpper",
    requestedId = "123",
    pageType = PageType.PRODUCT
  )

  @Test
  @DisplayName("Test the url classes functions")
  fun testUrlClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testUrlClassesFunctions")

    suite.test("testUrlKeysToJson") { testContext ->
      val result = testUrlKey.toDocument()
      testContext.assertEquals(testUrlKey.urlKey, result.getString("_id"))
      testContext.assertEquals(testUrlKey.upperKey, result.getString("upper_key"))
    }.test("testUrlKeysFromJson") { testContext ->
      val urlKeyJson = testUrlKey.toDocument()
      val urlKey = UrlKeys.fromJson(urlKeyJson)
      testContext.assertEquals(testUrlKey.urlKey, urlKey.urlKey)
      testContext.assertEquals(testUrlKey.upperKey, urlKey.upperKey)
    }

    suite.run(vertx).handler { res ->
      if (res.succeeded()) {
        context.completeNow()
      } else {
        context.failNow(res.cause())
      }
    }
  }

  @BeforeEach
  @DisplayName("Add the URL key to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(UrlKeys::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      UrlJdbcVerticle::class.qualifiedName.toString(),
      UrlJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<UrlKeys>("process.url.createUrlKey", testUrlKey, urlKeysDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.urlKey == testUrlKey.urlKey)
          assert(result.upperKey == testUrlKey.upperKey)
          assert(result.requestedId == testUrlKey.requestedId)
          assert(result.pageType == testUrlKey.pageType)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a URL key by key from the database")
  fun testGetUrlByKey(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.url.getUrlByKey", testUrlKey.urlKey).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = UrlKeys.fromJson(message.body())
      vertxTestContext.verify { ->
        assert(result.urlKey == testUrlKey.urlKey)
        assert(result.upperKey == testUrlKey.upperKey)
        assert(result.requestedId == testUrlKey.requestedId)
        assert(result.pageType == testUrlKey.pageType)
        vertxTestContext.completeNow()
      }
    }
  }

  @Test
  @DisplayName("Test updating a URL key in the database")
  fun testUpdateUrlKey(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedUrlKey = testUrlKey.copy(pageType = PageType.CATEGORY)
    eventBus.request<UrlKeys>("process.url.updateUrlKey", updatedUrlKey, urlKeysDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.pageType == updatedUrlKey.pageType)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the URL key from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.url.deleteUrlKey", testUrlKey.urlKey).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Url key deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
