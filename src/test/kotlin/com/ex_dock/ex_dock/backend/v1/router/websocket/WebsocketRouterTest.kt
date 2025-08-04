package com.ex_dock.ex_dock.backend.v1.router.websocket

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.load
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
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
class WebsocketRouterTest {
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
  @DisplayName("Test EventBus broadcastError")
  fun testBroadcastError(vertx: Vertx, context: VertxTestContext) {
    val errorPayload = JsonObject().put("code", 500).put("message", "Internal Server Error")
    val errorMessage = JsonObject()
      .put("targetType", "BROADCAST")
      .put("errorPayload", errorPayload)

    vertx.eventBus().consumer<JsonObject>("process.websocket.broadcastError") { message ->
      assertEquals(errorMessage, message.body())
      context.completeNow()
    }

    vertx.eventBus().send("process.websocket.broadcastError", errorMessage)
    context.completeNow()
  }
}
