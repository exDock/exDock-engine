package com.ex_dock.ex_dock.database.backend_block

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
class BackendBlockJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val backendBlockDeliveryOptions = DeliveryOptions().setCodecName("BlockInfoCodec")
  private val testBackendBlock = BlockInfo(
    blockId = "123",
    pageName = "testPage",
    productId = "1",
    categoryId = "1",
    blockName = "testBlock",
    blockType = "testType",
    blockAttributes = listOf(
      BlockAttribute(
        attributeId = "1",
        attributeName = "testAttribute",
        attributeType = "testAttributeType"
      )
    )
  )

  @BeforeEach
  @DisplayName("Add the backend block to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(BlockInfo::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      BackendBlockJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<BlockInfo>("process.backendBlock.createBackendBlock", testBackendBlock, backendBlockDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.blockId == testBackendBlock.blockId)
          assert(result.pageName == testBackendBlock.pageName)
          assert(result.blockName == testBackendBlock.blockName)
          assert(result.blockType == testBackendBlock.blockType)
          assert(result.blockAttributes.size == 1)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a backend block by id from the database")
  fun testGetBackendBlockById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.backendBlock.getBackendBlockById", testBackendBlock.blockId).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = BlockInfo.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.blockId == testBackendBlock.blockId)
          assert(result.pageName == testBackendBlock.pageName)
          assert(result.blockName == testBackendBlock.blockName)
          assert(result.blockType == testBackendBlock.blockType)
          assert(result.blockAttributes.size == 1)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating a backend block in the database")
  fun testUpdateBackendBlock(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedBackendBlock = testBackendBlock.copy(blockName = "updatedBlockName")
    eventBus.request<BlockInfo>("process.backendBlock.editBackendBlock", updatedBackendBlock, backendBlockDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.blockName == updatedBackendBlock.blockName)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the backend block from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.backendBlock.deleteBackendBlock", testBackendBlock.blockId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Backend Block deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
