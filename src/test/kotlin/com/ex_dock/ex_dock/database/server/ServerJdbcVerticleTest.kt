package com.ex_dock.ex_dock.database.server

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ServerJdbcVerticleTest {
  private val numTests = 1
  private lateinit var eventBus: EventBus
  private val serverDataDataDeliveryOptions = DeliveryOptions().setCodecName("ServerDataDataCodec")
  private val serverVersionDataDeliveryOptions = DeliveryOptions().setCodecName("ServerVersionDataCodec")
  private val serverDataList: MutableList<ServerDataData> = emptyList<ServerDataData>().toMutableList()
  private var serverVersionList: MutableList<ServerVersionData> = emptyList<ServerVersionData>().toMutableList()

  private var serverData = ServerDataData(
    "server_data_key",
    "server_data_value"
  )

  private var serverVersion = ServerVersionData(
    1,
    1,
    1,
    "test_name",
    "test_description"
  )

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(ServerDataData::class))
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(ServerVersionData::class))
    serverDataList.add(serverData)
    serverVersionList.add(serverVersion)

    deployWorkerVerticleHelper(vertx,
      ServerJDBCVerticle::class.qualifiedName.toString(), 5, 5).onComplete {
        eventBus.request<ServerDataData>("process.server.createServerData", serverData, serverDataDataDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete {createServerDataMsg ->
          assert(createServerDataMsg.succeeded())
          assertEquals(createServerDataMsg.result().body(), serverData)

          eventBus.request<ServerVersionData>("process.server.createServerVersion", serverVersion, serverVersionDataDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createServerVersionMsg ->
            assert(createServerVersionMsg.succeeded())
            assertEquals(createServerVersionMsg.result().body(), serverVersion)

            testContext.completeNow()
          }
        }
    }
  }

  @Test
  fun getAllServerData(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      eventBus.request<MutableList<ServerDataData>>("process.server.getAllServerData", "").onFailure {
        testContext.failNow(it)
      }.onComplete {
        assert(it.succeeded())
        assertEquals(it.result().body(), serverDataList)
      }
    }

    testContext.completeNow()
  }

  @Test
  fun getServerDataByKey(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      eventBus.request<ServerDataData>("process.server.getServerByKey", serverData.key).onFailure {
        testContext.failNow(it)
      }.onComplete {
        assert(it.succeeded())
        assertEquals(it.result().body(), serverData)
      }
    }

    testContext.completeNow()
  }

  @Test
  fun updateServerData(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      val updatedServerData = ServerDataData(
        serverData.key,
        "updated_server_data_value"
      )

      eventBus.request<ServerDataData>("process.server.updateServerData", updatedServerData, serverDataDataDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { updateServerMsg ->
        assert(updateServerMsg.succeeded())
        assertEquals(updateServerMsg.result().body(), updatedServerData)

        eventBus.request<ServerDataData>("process.server.getServerByKey", updatedServerData.key)
          .onFailure {
            testContext.failNow(it)
          }.onComplete {
          assert(it.succeeded())
          assertEquals(it.result().body(), updatedServerData)
        }
      }
    }

    testContext.completeNow()
  }

  @Test
  fun getAllServerVersions(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      eventBus.request<MutableList<ServerVersionData>>("process.server.getAllServerVersions", "").onFailure {
        testContext.failNow(it)
      }.onComplete { getAllServerVersionsMsg ->
        assert(getAllServerVersionsMsg.succeeded())
        assertEquals(
          getAllServerVersionsMsg.result().body(),
          serverVersionList)
      }
    }

    testContext.completeNow()
  }

  @Test
  fun getServerVersionById(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      eventBus.request<ServerVersionData>("process.server.getServerVersionByKey", serverVersion, serverVersionDataDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getServerVersionMsg ->
        assert(getServerVersionMsg.succeeded())
        assertEquals(getServerVersionMsg.result().body(), serverVersion)
      }
    }

    testContext.completeNow()
  }

  @Test
  fun updateServerVersion(vertx: Vertx, testContext: VertxTestContext) {
    for (i in 0..numTests) {
      val updatedServerVersion = ServerVersionData(
        serverVersion.major,
        serverVersion.minor,
        serverVersion.patch,
        "updated_test_name",
        "updated_test_description"
      )

      eventBus.request<ServerDataData>("process.server.updateServerVersion", updatedServerVersion, serverVersionDataDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { updateServerVersionMsg ->
        assert(updateServerVersionMsg.succeeded())
        assertEquals(updateServerVersionMsg.result().body(), updatedServerVersion)

        eventBus.request<ServerDataData>("process.server.getServerVersionByKey", updatedServerVersion).onFailure {
          testContext.failNow(it)
        }.onComplete {
          assert(it.succeeded())
          assertEquals(it.result().body(), updatedServerVersion)
        }
      }
    }

    testContext.completeNow()
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.server.deleteServerData", serverData.key).onFailure {
      testContext.failNow(it)
    }.onComplete {
      assert(it.succeeded())
      assertEquals(it.result().body(), "Server data deleted successfully!")
      eventBus.request<String>("process.server.deleteServerVersion", serverVersion, serverVersionDataDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteServerVersionMsg ->
        assert(deleteServerVersionMsg.succeeded())
        assertEquals(deleteServerVersionMsg.result().body(), "Server version deleted successfully!")

        testContext.completeNow()
      }
    }
  }
}
