package com.ex_dock.ex_dock.frontend.cache

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.load
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(VertxExtension::class)
class CacheVerticleTest {
  var accessToken: String = ""
  var host = ""
  var port = 0

  @BeforeEach
  @DisplayName("Deploying Server")
  fun setup(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    vertx.eventBus().registerGenericCodec(Map::class)
    deployWorkerVerticleHelper(
      vertx,
      MainVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure {
      context.failNow(it)
    }.onSuccess {
      val props = Properties().load()
      val loginCredentials = JsonObject()
        .put("email", props.getProperty("ADMIN_EMAIL"))
        .put("password", props.getProperty("ADMIN_PASSWORD"))
      host = props.getProperty("HOST")
      port = props.getProperty("FRONTEND_PORT").toInt()


      client.request(HttpMethod.POST, port, host, "/api/v1/token").compose { request ->
        request.putHeader("Content-Type", "application/json")
        request.send(loginCredentials.encode()).compose(HttpClientResponse::body).onFailure {
          context.failNow(it)
        }.onSuccess { body ->
          val response = body.toJsonObject()
          val tokens = JsonObject(response.getString("tokens"))
          accessToken = tokens.getString("access_token")
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test process.cache.requestData")
  fun testRequestData(vertx: Vertx, context: VertxTestContext) {
    val requestedData = "accounts;categories"
    val mockAccountData = listOf(JsonObject().put("id", "acc1"), JsonObject().put("id", "acc2"))
    val mockCategoryData = listOf(JsonObject().put("id", "cat1"), JsonObject().put("id", "cat2"))

    // Mock EventBus consumers that CacheVerticle calls
    vertx.eventBus().consumer<String>("process.account.getAllFullUserInfo") { message ->
      message.reply(io.vertx.core.json.Json.encode(mockAccountData))
    }
    vertx.eventBus().consumer<String>("process.categories.getAllFullInfo") { message ->
      message.reply(io.vertx.core.json.Json.encode(mockCategoryData))
    }

    vertx.eventBus().request<Map<String, List<Any>>>("process.cache.requestData", requestedData).onComplete { ar ->
      assertTrue(ar.succeeded())
      val result = ar.result().body()
      assertTrue(result.containsKey("accounts"))
      assertTrue(result.containsKey("categories"))
      assertTrue(mockAccountData.size >= result["accounts"]!!.size)
      assertTrue(mockCategoryData.size >= result["categories"]!!.size)
      context.completeNow()
    }
  }

  @Test
  @DisplayName("Test process.cache.invalidateKey")
  fun testInvalidateKey(vertx: Vertx, context: VertxTestContext) {
    val cacheKeyToInvalidate = "accounts"

    // First, request data to populate the cache
    vertx.eventBus().consumer<String>("process.account.getAllFullUserInfo") { message ->
      message.reply(io.vertx.core.json.Json.encode(listOf(JsonObject().put("id", "acc1"))))
    }

    vertx.eventBus().request<Map<String, List<Any>>>("process.cache.requestData", cacheKeyToInvalidate).onComplete { ar ->
      assertTrue(ar.succeeded())

      // Now, invalidate the key
      vertx.eventBus().request<String>("process.cache.invalidateKey", cacheKeyToInvalidate).onComplete { invalidateAr ->
        assertTrue(invalidateAr.succeeded())
        assertEquals("", invalidateAr.result().body())
        context.completeNow()
      }
    }
  }
}
