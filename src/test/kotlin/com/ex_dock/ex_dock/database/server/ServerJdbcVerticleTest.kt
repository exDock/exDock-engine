package com.ex_dock.ex_dock.database.server

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ServerJDBCVerticleTest {
  private lateinit var eventBus: EventBus
  private val serverDataDeliveryOptions = DeliveryOptions().setCodecName("ServerDataDataCodec")
  private val testServerData = ServerDataData(
    key = "testKey",
    value = "testValue"
  )

  @BeforeEach
  @DisplayName("Add the server data to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(ServerDataData::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      ServerJDBCVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<ServerDataData>("process.server.createServerData", testServerData, serverDataDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.key == testServerData.key)
          assert(result.value == testServerData.value)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting server data by key from the database")
  fun testGetServerDataByKey(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.server.getServerDataByKey", testServerData.key).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = ServerDataData.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.key == testServerData.key)
          assert(result.value == testServerData.value)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating server data in the database")
  fun testUpdateServerData(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedServerData = testServerData.copy(value = "updatedValue")
    eventBus.request<ServerDataData>("process.server.updateServerData", updatedServerData, serverDataDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.value == updatedServerData.value)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the server data from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.server.deleteServerData", testServerData.key).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Server data deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
