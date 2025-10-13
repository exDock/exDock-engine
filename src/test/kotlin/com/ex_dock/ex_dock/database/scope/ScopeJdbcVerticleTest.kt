package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ScopeJdbcVerticleTest {

  private lateinit var eventBus: EventBus
  private val idsToCleanup = mutableListOf<String>()

  // Test data
  private val websiteJson = JsonObject()
    .put("scopeName", "Test Website")
    .put("scopeKey", "test_website")

  private lateinit var testWebsiteId: String

  @BeforeEach
  @DisplayName("Deploy Verticle and Create a Base Website Scope")
  fun setup(vertx: Vertx, context: VertxTestContext) {
    eventBus = vertx.eventBus()
    // The List codec is still needed for functions that return multiple results
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      ScopeJdbcVerticle::class.qualifiedName.toString(),
      ScopeJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      context.failNow(err)
    }.onSuccess {
      // Create a base website for other tests to use
      eventBus.request<String>("process.scope.create.website", websiteJson).onFailure {
        context.failNow(it)
      }.onSuccess { message ->
        testWebsiteId = message.body()
        idsToCleanup.add(testWebsiteId) // Ensure it gets cleaned up
        context.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove Scopes from the Database")
  fun tearDown(vertx: Vertx, context: VertxTestContext) {
    val checkpoint = context.checkpoint(idsToCleanup.size)
    if (idsToCleanup.isEmpty()) {
      context.completeNow()
      return
    }

    idsToCleanup.forEach { scopeId ->
      eventBus.request<String>("process.scope.deleteScope", scopeId).onComplete {
        checkpoint.flag()
      }
    }
  }

  @Test
  @Order(1)
  @DisplayName("Test creating a valid website scope")
  fun testCreateWebsite(context: VertxTestContext) {
    val newWebsite = JsonObject()
      .put("scopeName", "Another Website")
      .put("scopeKey", "another_website")

    eventBus.request<String>("process.scope.create.website", newWebsite).onFailure {
      context.failNow(it)
    }.onSuccess { message ->
      val newId = message.body()
      context.verify {
        Assertions.assertNotNull(newId)
      }
      idsToCleanup.add(newId) // Add for cleanup
      context.completeNow()
    }
  }

  @Test
  @Order(2)
  @DisplayName("Test creating a valid store-view scope")
  fun testCreateStoreView(context: VertxTestContext) {
    val storeViewJson = JsonObject()
      .put("name", "Test Store View")
      .put("key", "test_store_view")
      .put("websiteId", testWebsiteId)

    eventBus.request<String>("process.scope.create.store-view", storeViewJson).onFailure {
      context.failNow(it)
    }.onSuccess { message ->
      val newId = message.body()
      context.verify {
        Assertions.assertNotNull(newId)
      }
      idsToCleanup.add(newId)
      context.completeNow()
    }
  }

  @Test
  @Order(3)
  @DisplayName("Test creating a store-view with a non-existent websiteId fails")
  fun testCreateStoreViewWithInvalidWebsiteId(context: VertxTestContext) {
    val storeViewJson = JsonObject()
      .put("name", "Invalid Store View")
      .put("key", "invalid_store_view")
      .put("websiteId", "nonExistentId123")

    eventBus.request<String>("process.scope.create.store-view", storeViewJson).onSuccess {
      context.failNow("Should have failed for invalid websiteId")
    }.onFailure {
      context.verify {
        Assertions.assertTrue(it.message?.contains("does not exist") ?: false)
        context.completeNow()
      }
    }
  }

  @Test
  @Order(4)
  @DisplayName("Test getting a scope by its ID")
  fun testGetScopeById(context: VertxTestContext) {
    eventBus.request<JsonObject>("process.scope.getScopeByWebsiteId", testWebsiteId).onFailure {
      context.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      context.verify {
        Assertions.assertEquals(testWebsiteId, result.getString("_id"))
        Assertions.assertEquals(websiteJson.getString("scopeName"), result.getString("scopeName"))
        Assertions.assertEquals("website", result.getString("scopeType"))
        context.completeNow()
      }
    }
  }

  @Test
  @Order(5)
  @DisplayName("Test getting all scopes")
  fun testGetAllScopes(context: VertxTestContext) {
    eventBus.request<List<JsonObject>>("process.scope.getAllScopes", null).onFailure {
      context.failNow(it)
    }.onSuccess { message ->
      val results = message.body()
      results.size
      context.verify {
        // At least the website from setup should be present
        Assertions.assertTrue(results.isNotEmpty())
        val firstResult = results[0]
        Assertions.assertNotNull(firstResult.getString("_id"))
        context.completeNow()
      }
    }
  }

  /*
   * NOTE: A test for 'editScope' has been omitted.
   * The `editScope` function in `ScopeJdbcVerticle` has not yet been refactored.
   * It still expects a `Scope` object, which is deprecated.
   * A new test should be written once `editScope` is updated to work with JsonObject payloads.
   */
}