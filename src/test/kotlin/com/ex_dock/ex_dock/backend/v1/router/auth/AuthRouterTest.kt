package com.ex_dock.ex_dock.backend.v1.router.auth

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.load
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(VertxExtension::class)
class AuthRouterTest {
  var accessToken: String = ""
  var refreshToken: String = ""
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
          refreshToken = tokens.getString("refresh_token")
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test the token request without credentials")
  fun testTokenNoCredentials(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.POST, port, host, "/api/v1/token").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(400, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Bad Request", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test the token request with invalid credentials")
  fun testTokenInvalidCredentials(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val loginCredentials = JsonObject()
      .put("email", "invalid")
      .put("password", "invalid")

    client.request(HttpMethod.POST, port, host, "/api/v1/token").compose { request ->
      request.putHeader("Content-Type", "application/json")
      request.send(loginCredentials.encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(401, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Unauthorized", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test the refresh endpoint without token")
  fun testRefreshNoToken(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.POST, port, host, "/api/v1/refresh").onFailure {
      context.failNow(it)
    }.onSuccess { request ->
      request.putHeader("Content-Type", "application/json")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(400, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Bad Request", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test the refresh endpoint with invalid token")
  fun testRefreshInvalidToken(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val refreshCredentials = JsonObject()
      .put("refresh_token", "invalid")

    client.request(HttpMethod.POST, port, host, "/api/v1/refresh").onFailure {
      context.failNow(it)
    }.onSuccess { request ->
      request.putHeader("Content-Type", "application/json")
      request.send(refreshCredentials.encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(401, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Unauthorized", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test the refresh endpoint with valid token")
  fun testRefreshValidToken(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val refreshCredentials = JsonObject()
      .put("refresh_token", refreshToken)

    client.request(HttpMethod.POST, port, host, "/api/v1/refresh").onFailure {
      context.failNow(it)
    }.onSuccess { request ->
      request.putHeader("Content-Type", "application/json")
      request.send(refreshCredentials.encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            val tokens = JsonObject(buffer)
            assert(accessToken != tokens.getString("access_token"))
            assert(tokens.getString("refresh_token") != refreshToken)
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test the ping endpoint")
  fun testPing(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/ping").onFailure {
      context.failNow(it)
    }.onSuccess { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Server responded!", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }
}
