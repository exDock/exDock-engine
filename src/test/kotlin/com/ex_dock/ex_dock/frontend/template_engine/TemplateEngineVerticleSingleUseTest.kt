package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.database.sales.Order
import com.ex_dock.ex_dock.helper.AsyncExDockCache
import com.ex_dock.ex_dock.helper.CacheData
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CompletableFuture

@ExtendWith(VertxExtension::class)
class TemplateEngineVerticleSingleUseTest {
  private val mockDataCache: AsyncExDockCache<Order> = mock()
  private val dataAccessor = DataAccessor(
    productCache = mock(),
    categoryCache = mock(),
    creditMemoCache = mock(),
    invoiceCache = mock(),
    orderCache = mockDataCache,
    shipmentCache = mock(),
    transactionCache = mock(),
  )
  lateinit var eventBus: EventBus

  @BeforeEach
  fun deployEventBus(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    testContext.completeNow()
  }

  @BeforeEach
  fun deployTemplateEngineVerticle(vertx: Vertx, testContext: VertxTestContext) {
    deployWorkerVerticleHelper(
      vertx,
      TemplateEngineVerticle::class.qualifiedName.toString(),
      TemplateEngineVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onSuccess {
      testContext.completeNow()
    }.onFailure { throwable ->
      testContext.failNow(throwable)
    }
  }

  @Test
  fun testSingleUseTemplate(vertx: Vertx, testContext: VertxTestContext) {
    val checkpoint = testContext.checkpoint()

    whenever(mockDataCache.getById("test")).thenReturn(
      CompletableFuture.completedFuture(
        CacheData(
          data = Order(
            orderId = "test",
            language = "en",
            date = "10-10-2010",
            customerId = "test",
            status = "test",
            items = emptyList()
          ),
          hits = 0
        )
      )
    )

    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ order.orderId }}</test>")
      .put("orderId", "test")

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      checkpoint.flag()
    }
  }

  @Test
  fun testAbundantData(vertx: Vertx, testContext: VertxTestContext) {
    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ name }}</test>")
      .put("name", "testName")
      .put("abundantName", "testAbundantName")

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      testContext.completeNow()
    }
  }

  @Test
  fun testMissingData(vertx: Vertx, testContext: VertxTestContext) {
    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ name }}</test>")

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      testContext.completeNow()
    }
  }

  @Test
  fun testSubData(vertx: Vertx, testContext: VertxTestContext) {
    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ name.subData }}</test>")
      .put("name", JsonObject().put("subData", "testSubData"))

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      testContext.completeNow()
    }
  }

  @Test
  fun testRequestMapValue(vertx: Vertx, testContext: VertxTestContext) {
    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ name }}</test>")
      .put("name", JsonObject().put("subData", "testSubData"))

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      testContext.completeNow()
    }
  }

  @Test
  fun testPickNewestData(vertx: Vertx, testContext: VertxTestContext) {
    val singleUseTemplateData = JsonObject()
      .put("templateData", "<test>{{ name }}</test>")
      .put("name", "testData1")
      .put("name", "testData2")

    val expectedResult = "<test></test>"

    eventBus.request<String>(
      "template.generate.singleUse",
      singleUseTemplateData,
    ).onFailure {
      testContext.failNow(it)
    }.onComplete { message ->
      assert(message.succeeded())
      assertEquals(
        "String",
        message.result().body()::class.simpleName,
        "Received class isn't String"
      )

      val result: String = message.result().body()
      testContext.verify {
        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
      }

      // Mark as successful
      testContext.completeNow()
    }
  }
}
