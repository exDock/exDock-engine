package com.ex_dock.ex_dock.backend

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
import java.util.*

@ExtendWith(VertxExtension::class)
class BackendRouterTest {
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
  @DisplayName("Test the about page")
  fun testAboutPage(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/about").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        context.verify {
          assert(response.statusCode() == 200)
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assert(buffer.toString() == "about page for the APIs")
            context.completeNow()
          }
        }
      }
    }
  }
}
