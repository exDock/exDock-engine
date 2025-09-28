package com.ex_dock.ex_dock.database.scope

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
class ScopeJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val scopeDeliveryOptions = DeliveryOptions().setCodecName("ScopeCodec")
  private var testScope = Scope(
    scopeId = "123",
    websiteName = "testWebsite",
    storeViewName = "testStoreView"
  )

  @Test
  @DisplayName("Test the scope classes functions")
  fun testScopeClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testScopeClassesFunctions")

    suite.test("testScopeToJson") { testContext ->
      val result = testScope.toDocument()
      testContext.assertEquals(testScope.scopeId, result.getString("_id"))
      testContext.assertEquals(testScope.websiteName, result.getString("website_name"))
    }.test("testScopeFromJson") { testContext ->
      val scopeJson = testScope.toDocument()
      val scope = Scope.fromJson(scopeJson)
      testContext.assertEquals(testScope.scopeId, scope.scopeId)
      testContext.assertEquals(testScope.websiteName, scope.websiteName)
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
  @DisplayName("Add the scope to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(Scope::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      ScopeJdbcVerticle::class.qualifiedName.toString(),
      ScopeJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<Scope>("process.scope.createScope", testScope, scopeDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        testScope.scopeId = result.scopeId // Update testScope with the generated ID
        vertxTestContext.verify { ->
          assert(result.scopeId != null)
          assert(result.websiteName == testScope.websiteName)
          assert(result.storeViewName == testScope.storeViewName)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a scope by id from the database")
  fun testGetScopeById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.scope.getScopeByWebsiteId", testScope.scopeId).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = Scope.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.scopeId == testScope.scopeId)
          assert(result.websiteName == testScope.websiteName)
          assert(result.storeViewName == testScope.storeViewName)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating a scope in the database")
  fun testUpdateScope(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedScope = testScope.copy(websiteName = "updatedWebsite")
    eventBus.request<Scope>("process.scope.editScope", updatedScope, scopeDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.websiteName == updatedScope.websiteName)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the scope from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.scope.deleteScope", testScope.scopeId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Scope deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
