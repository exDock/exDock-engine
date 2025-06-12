package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.VerticleDeployHelper
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ProductCustomAttributesJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private val verticleDeployHelper = VerticleDeployHelper()

  private val productCustomAttributes = CustomProductAttributes(
    attributeKey = "test attribute key",
    scope = 2,
    name = "test name",
    type = convertStringToType("int"),
    multiselect = false,
    required = true
  )

  private val customProductAttributesDataDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private val customProductAttributesList: MutableList<CustomProductAttributes> = emptyList<CustomProductAttributes>().toMutableList()

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(CustomProductAttributes::class))
    ProductCustomAttributesJdbcVerticle::class.qualifiedName.toString()
    deployWorkerVerticleHelper(vertx,
      ProductCustomAttributesJdbcVerticle::class.qualifiedName.toString(), 5, 5).onFailure {
      testContext.failNow(it)
    }.onComplete {
      eventBus.request<Int>("process.attributes.createCustomAttribute", productCustomAttributes, customProductAttributesDataDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createCustomAttributeMsg ->
        assert(createCustomAttributeMsg.succeeded())
        assertEquals(productCustomAttributes, createCustomAttributeMsg.result().body())
        customProductAttributesList.add(productCustomAttributes)

        testContext.completeNow()
      }
    }
  }

  @Test
  fun getAllCustomAttributes(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<CustomProductAttributes>>("process.attributes.getAllCustomAttributes", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllCustomAttributesMsg ->
      assert(getAllCustomAttributesMsg.succeeded())
      assertEquals(customProductAttributesList, getAllCustomAttributesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun getCustomAttributeByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<CustomProductAttributes>("process.attributes.getCustomAttributeByKey",
      productCustomAttributes.attributeKey).onFailure {
      testContext.failNow(it)
    }.onComplete { getCustomAttributeByKeyMsg ->
      assert(getCustomAttributeByKeyMsg.succeeded())
      assertEquals(productCustomAttributes, getCustomAttributeByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun updateCustomAttribute(vertx: Vertx, testContext: VertxTestContext) {
    val updatedProductCustomAttributes = CustomProductAttributes(
      attributeKey = productCustomAttributes.attributeKey,
      scope = 2,
      name = "updated test name",
      type = convertStringToType("int"),
      multiselect = false,
      required = true
    )


    eventBus.request<CustomProductAttributes>("process.attributes.updateCustomAttribute",
      updatedProductCustomAttributes, customProductAttributesDataDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateCustomAttributeMsg ->
      assert(updateCustomAttributeMsg.succeeded())
      assertEquals(updatedProductCustomAttributes, updateCustomAttributeMsg.result().body())

      eventBus.request<CustomProductAttributes>("process.attributes.getCustomAttributeByKey",
        updatedProductCustomAttributes.attributeKey).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedCustomAttributeByKeyMsg ->
        assert(getUpdatedCustomAttributeByKeyMsg.succeeded())
        assertEquals(updatedProductCustomAttributes, getUpdatedCustomAttributeByKeyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.attributes.deleteCustomAttribute",
      productCustomAttributes.attributeKey).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteCustomAttributeMsg ->
      assert(deleteCustomAttributeMsg.succeeded())
      assertEquals("Custom attribute deleted successfully", deleteCustomAttributeMsg.result().body())

      testContext.completeNow()
    }
  }

  private fun convertStringToType(name: String): Type {
    return when (name) {
      "string" -> Type.STRING
      "bool" -> Type.BOOL
      "float" -> Type.FLOAT
      "int" -> Type.INT
      "money" -> Type.MONEY
      else -> throw IllegalArgumentException("Invalid type: $name")
    }
  }
}
