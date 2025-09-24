package com.ex_dock.ex_dock.backend.v1.router.file

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.load
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

@ExtendWith(VertxExtension::class)
class FileRouterTest {
  var accessToken: String = ""
  var host = ""
  var port = 0

  @BeforeEach
  @DisplayName("Deploying Server")
  fun setup(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
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
  @DisplayName("Test GET /file/getAll")
  fun testGetAllFiles(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/file/getAll").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("files"))
          assertTrue(jsonResponse.getJsonArray("files").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /file/getAll/:path for directory")
  fun testGetAllFilesFromDirectory(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/file/getAll/products").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("files"))
          assertTrue(jsonResponse.getJsonArray("files").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /file/getAll/:path for file")
  fun testGetFileContent(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/file/getAll/products%2F1%2Fproduct.json").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(400, response.statusCode())
        context.completeNow()
      }
    }
  }

  @Test
  @DisplayName("Test GET /file/getBlockData/:blockName")
  fun testGetBlockData(vertx: Vertx, context: VertxTestContext) {
    // Mock the EventBus response for getBackendBlocksByPageName
    vertx.eventBus().consumer<String>("process.backendBlock.getBackendBlocksByPageName") { message ->
      val mockResponse = JsonObject()
        .put("block_type", "testBlock")
        .put("attributes", JsonArray().add(JsonObject().put("attribute_name", "Files").put("attribute_id", "123").put("attribute_type", "file").put("current_attribute_value", JsonArray())))
      message.reply(io.vertx.core.json.Json.encode(listOf(mockResponse)))
    }

    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/file/getBlockData/testBlockName").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        context.completeNow()
      }
    }
  }
}
