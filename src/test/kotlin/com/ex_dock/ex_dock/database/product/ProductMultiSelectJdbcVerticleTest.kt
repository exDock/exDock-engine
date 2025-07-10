package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ProductMultiSelectJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val multiSelectBoolDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectBoolCodec")
  private val multiSelectFloatDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectFloatCodec")
  private val multiSelectIntDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectIntCodec")
  private val multiSelectMoneyDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectMoneyCodec")
  private val multiSelectStringDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectStringCodec")
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val customProductAttributeDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val msaBoolList: MutableList<MultiSelectBool> = emptyList<MultiSelectBool>().toMutableList()
  private val msaFloatList: MutableList<MultiSelectFloat> = emptyList<MultiSelectFloat>().toMutableList()
  private val msaIntList: MutableList<MultiSelectInt> = emptyList<MultiSelectInt>().toMutableList()
  private val msaMoneyList: MutableList<MultiSelectMoney> = emptyList<MultiSelectMoney>().toMutableList()
  private val msaStringList: MutableList<MultiSelectString> = emptyList<MultiSelectString>().toMutableList()
  private val msaInfoList: MutableList<MultiSelectInfo> = emptyList<MultiSelectInfo>().toMutableList()

  private var productId = -1

  private var product = Products(
    productId = productId,
    name = "Test Product",
    shortName = "TProduct",
    description = "Test Product Description",
    shortDescription = "TPDescription",
    sku = "sku",
    ean = "ean",
    manufacturer = "manufacturer",
  )

  private var customProductAttribute = CustomProductAttributes(
    attributeKey = "test attribute key",
    scope = 1,
    name = "Test Attribute",
    type = Type.INT,
    multiselect = false,
    required = true
  )

  private val msaBool = MultiSelectBool(
    attributeKey = "test attribute key",
    option = 1,
    value = true
  )

  private val msaFloat = MultiSelectFloat(
    attributeKey = "test attribute key",
    option = 1,
    value = 1.0f
  )

  private val msaString = MultiSelectString(
    attributeKey = "test attribute key",
    option = 1,
    value = "test value"
  )

  private val msaInt = MultiSelectInt(
    attributeKey = "test attribute key",
    option = 1,
    value = 1
  )

  private val msaMoney = MultiSelectMoney(
    attributeKey = "test attribute key",
    option = 1,
    value = 10.0
  )

  private var msaInfo = MultiSelectInfo(
    product = product,
    attributeKey = "test attribute key",
    multiSelectBool = msaBool.value,
    multiSelectFloat = msaFloat.value,
    multiSelectString = msaString.value,
    multiSelectInt = msaInt.value,
    multiSelectMoney = msaMoney.value
  )

  private var eav = Eav(
    productId = productId,
    attributeKey = "test attribute key",
  )


  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(CustomProductAttributes::class))
      .registerCodec(GenericCodec(Eav::class))
      .registerCodec(GenericCodec(MultiSelectBool::class))
      .registerCodec(GenericCodec(MultiSelectFloat::class))
      .registerCodec(GenericCodec(MultiSelectString::class))
      .registerCodec(GenericCodec(MultiSelectInt::class))
      .registerCodec(GenericCodec(MultiSelectMoney::class))
      .registerCodec(GenericCodec(MultiSelectInfo::class))
    Future.all(deployVerticles(vertx)).onFailure {
      testContext.failNow(it)
    }.onComplete {
      eventBus.request<Products>("process.products.createProduct", product, productDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createProductMsg ->
        product = createProductMsg.result().body()
        productId = product.productId
        assertEquals(createProductMsg.result().body(), product)

        eventBus.request<CustomProductAttributes>("process.attributes.createCustomAttribute", customProductAttribute, customProductAttributeDeliveryOptions)
          .onFailure {
            testContext.failNow(it)
          }.onComplete { createCustomAttributeMsg ->
            assert(createCustomAttributeMsg.succeeded())
            assertEquals(createCustomAttributeMsg.result().body(), customProductAttribute)

            eventBus.request<MultiSelectBool>("process.multiSelect.createMultiSelectAttributesBool", msaBool, multiSelectBoolDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { createMsaBoolMsg ->
              assert(createMsaBoolMsg.succeeded())
              assertEquals(msaBool, createMsaBoolMsg.result().body())

              eventBus.request<MultiSelectFloat>("process.multiSelect.createMultiSelectAttributesFloat", msaFloat, multiSelectFloatDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { createMsaFloatMsg ->
                assert(createMsaFloatMsg.succeeded())
                assertEquals(msaFloat, createMsaFloatMsg.result().body())

                eventBus.request<MultiSelectString>("process.multiSelect.createMultiSelectAttributesString", msaString, multiSelectStringDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { createMsaStringMsg ->
                  assert(createMsaStringMsg.succeeded())
                  assertEquals(msaString, createMsaStringMsg.result().body())

                  eventBus.request<MultiSelectInt>("process.multiSelect.createMultiSelectAttributesInt", msaInt, multiSelectIntDeliveryOptions).onFailure {
                    testContext.failNow(it)
                  }.onComplete { createMsaIntMsg ->
                    assert(createMsaIntMsg.succeeded())
                    assertEquals(msaInt, createMsaIntMsg.result().body())

                    eventBus.request<MultiSelectMoney>("process.multiSelect.createMultiSelectAttributesMoney", msaMoney, multiSelectMoneyDeliveryOptions).onFailure {
                      testContext.failNow(it)
                    }.onComplete { createMsaMoneyMsg ->
                      assert(createMsaMoneyMsg.succeeded())
                      assertEquals(msaMoney, createMsaMoneyMsg.result().body())

                      msaInfo.product.productId = product.productId
                      eav.productId = product.productId
                      msaBoolList.add(msaBool)
                      msaFloatList.add(msaFloat)
                      msaStringList.add(msaString)
                      msaIntList.add(msaInt)
                      msaMoneyList.add(msaMoney)
                      msaInfoList.add(msaInfo)

                      eventBus.request<String>("process.eavGlobal.createEavGlobal", eav, eavDeliveryOptions).onFailure {
                        testContext.failNow(it)
                      }.onComplete { createEavGlobalMsg ->
                        assert(createEavGlobalMsg.succeeded())
                        assertEquals(eav, createEavGlobalMsg.result().body())

                        testContext.completeNow()
                      }
                    }
                  }
                }
              }
            }
          }
      }
    }
  }

  @Test
  fun testGetAllMsaBool(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectBool>>("process.multiSelect.getAllMultiSelectAttributesBool", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllMsaBoolMsg ->
      assert(getAllMsaBoolMsg.succeeded())
      assertEquals(msaBoolList, getAllMsaBoolMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetMsaBoolByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MultiSelectBool>("process.multiSelect.getMultiSelectAttributesBoolByKey", msaBool, multiSelectBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaBool, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateMultiSelectBool(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMsaBool= MultiSelectBool(
      attributeKey = msaBool.attributeKey,
      option = msaBool.option,
      value = false
    )

    eventBus.request<MultiSelectBool>("process.multiSelect.updateMultiSelectAttributesBool", updatedMsaBool, multiSelectBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateMsaBoolMsg ->
      assert(updateMsaBoolMsg.succeeded())
      assertEquals(updatedMsaBool, updateMsaBoolMsg.result().body())

      eventBus.request<MultiSelectBool>("process.multiSelect.getMultiSelectAttributesBoolByKey", updatedMsaBool, multiSelectBoolDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getMsaByKeyMsg ->
          assert(getMsaByKeyMsg.succeeded())
          assertEquals(updatedMsaBool, getMsaByKeyMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllMultiSelectAttributesFloat(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectFloat>>("process.multiSelect.getAllMultiSelectAttributesFloat", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllMsaFloatMsg ->
      assert(getAllMsaFloatMsg.succeeded())
      assertEquals(msaFloatList, getAllMsaFloatMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetMsaFloatByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MultiSelectFloat>("process.multiSelect.getMultiSelectAttributesFloatByKey", msaFloat, multiSelectFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaFloat, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateMultiSelectFloat(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMsaFloat = MultiSelectFloat(
      attributeKey = msaFloat.attributeKey,
      option = msaFloat.option,
      value = 100.5f
    )

    eventBus.request<MultiSelectFloat>("process.multiSelect.updateMultiSelectAttributesFloat", updatedMsaFloat, multiSelectFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateMsaFloatMsg ->
      assert(updateMsaFloatMsg.succeeded())
      assertEquals(updatedMsaFloat, updateMsaFloatMsg.result().body())

      eventBus.request<MultiSelectFloat>("process.multiSelect.getMultiSelectAttributesFloatByKey", updatedMsaFloat, multiSelectFloatDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedMsaFloatMsg ->
          assert(getUpdatedMsaFloatMsg.succeeded())
          assertEquals(updatedMsaFloat, getUpdatedMsaFloatMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllMultiSelectAttributesString(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectString>>("process.multiSelect.getAllMultiSelectAttributesString", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllMsaStringMsg ->
      assert(getAllMsaStringMsg.succeeded())
      assertEquals(msaStringList, getAllMsaStringMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetMsaStringByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MultiSelectString>("process.multiSelect.getMultiSelectAttributesStringByKey", msaString, multiSelectStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaString, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateMultiSelectString(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMsaString = MultiSelectString(
      attributeKey = msaString.attributeKey,
      option = msaString.option,
      value = "Updated String"
    )

    eventBus.request<MultiSelectString>("process.multiSelect.updateMultiSelectAttributesString", updatedMsaString, multiSelectStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateMsaStringMsg ->
      assert(updateMsaStringMsg.succeeded())
      assertEquals(updatedMsaString, updateMsaStringMsg.result().body())

      eventBus.request<MultiSelectString>("process.multiSelect.getMultiSelectAttributesStringByKey", updatedMsaString, multiSelectStringDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedMsaStringMsg ->
          assert(getUpdatedMsaStringMsg.succeeded())
          assertEquals(updatedMsaString, getUpdatedMsaStringMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllMultiSelectAttributesInt(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectInt>>("process.multiSelect.getAllMultiSelectAttributesInt", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllMsaIntMsg ->
      assert(getAllMsaIntMsg.succeeded())
      assertEquals(msaIntList, getAllMsaIntMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetMsaIntByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MultiSelectInt>("process.multiSelect.getMultiSelectAttributesIntByKey", msaInt, multiSelectIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaInt, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateMultiSelectInt(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMsaInt = MultiSelectInt(
      attributeKey = msaInt.attributeKey,
      option = msaInt.option,
      value = 100
    )

    eventBus.request<MultiSelectInt>("process.multiSelect.updateMultiSelectAttributesInt", updatedMsaInt, multiSelectIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateMsaIntMsg ->
      assert(updateMsaIntMsg.succeeded())
      assertEquals(updatedMsaInt, updateMsaIntMsg.result().body())

      eventBus.request<MultiSelectInt>("process.multiSelect.getMultiSelectAttributesIntByKey", updatedMsaInt, multiSelectIntDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedMsaIntMsg ->
          assert(getUpdatedMsaIntMsg.succeeded())
          assertEquals(updatedMsaInt, getUpdatedMsaIntMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllMultiSelectMoney(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectMoney>>("process.multiSelect.getAllMultiSelectAttributesMoney", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllMsaMoneyMsg ->
      assert(getAllMsaMoneyMsg.succeeded())
      assertEquals(msaMoneyList, getAllMsaMoneyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetMsaMoneyByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MultiSelectMoney>("process.multiSelect.getMultiSelectAttributesMoneyByKey", msaMoney, multiSelectMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaMoney, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateMultiSelectMoney(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMsaMoney = MultiSelectMoney(
      attributeKey = msaMoney.attributeKey,
      option = msaMoney.option,
      value = 100.50
    )

    eventBus.request<MultiSelectMoney>("process.multiSelect.updateMultiSelectAttributesMoney", updatedMsaMoney, multiSelectMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateMsaMoneyMsg ->
      assert(updateMsaMoneyMsg.succeeded())
      assertEquals(updatedMsaMoney, updateMsaMoneyMsg.result().body())

      eventBus.request<MultiSelectMoney>("process.multiSelect.getMultiSelectAttributesMoneyByKey", updatedMsaMoney, multiSelectMoneyDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedMsaMoneyMsg ->
          assert(getUpdatedMsaMoneyMsg.succeeded())
          assertEquals(updatedMsaMoney, getUpdatedMsaMoneyMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllMultiSelectAttributesInfo(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<MutableList<MultiSelectInfo>>("process.multiSelect.getAllMultiSelectAttributesInfo", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllMsaInfoMsg ->
//      assert(getAllMsaInfoMsg.succeeded())
//      assertEquals(msaInfoList, getAllMsaInfoMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetMsaInfoByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<MultiSelectInfo>>("process.multiSelect.getMultiSelectAttributesInfoByKey", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getMsaByKeyMsg ->
      assert(getMsaByKeyMsg.succeeded())
      assertEquals(msaInfoList, getMsaByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.eavGlobal.deleteEavGlobal", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteEavGlobalMsg ->
      assert(deleteEavGlobalMsg.succeeded())
      assertEquals("EAV global deleted successfully", deleteEavGlobalMsg.result().body())
      eventBus.request<String>(
        "process.multiSelect.deleteMultiSelectAttributesBool",
        msaBool,
        multiSelectBoolDeliveryOptions
      ).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteMsaBoolMsg ->
        assert(deleteMsaBoolMsg.succeeded())
        assertEquals("Multi-select attribute bool deleted successfully", deleteMsaBoolMsg.result().body())

        eventBus.request<String>(
          "process.multiSelect.deleteMultiSelectAttributesFloat",
          msaFloat,
          multiSelectFloatDeliveryOptions
        ).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteMsaFloatMsg ->
          assert(deleteMsaFloatMsg.succeeded())
          assertEquals("Multi-select attribute float deleted successfully", deleteMsaFloatMsg.result().body())

          eventBus.request<String>(
            "process.multiSelect.deleteMultiSelectAttributesString",
            msaString,
            multiSelectStringDeliveryOptions
          ).onFailure {
            testContext.failNow(it)
          }.onComplete { deleteMsaStringMsg ->
            assert(deleteMsaStringMsg.succeeded())
            assertEquals("Multi-select attribute string deleted successfully", deleteMsaStringMsg.result().body())

            eventBus.request<String>(
              "process.multiSelect.deleteMultiSelectAttributesInt",
              msaInt,
              multiSelectIntDeliveryOptions
            ).onFailure {
              testContext.failNow(it)
            }.onComplete { deleteMsaIntMsg ->
              assert(deleteMsaIntMsg.succeeded())
              assertEquals("Multi-select attribute int deleted successfully", deleteMsaIntMsg.result().body())

              eventBus.request<String>(
                "process.multiSelect.deleteMultiSelectAttributesMoney",
                msaMoney,
                multiSelectMoneyDeliveryOptions
              ).onFailure {
                testContext.failNow(it)
              }.onComplete { deleteMsaMoneyMsg ->
                assert(deleteMsaMoneyMsg.succeeded())
                assertEquals("Multi-select attribute money deleted successfully", deleteMsaMoneyMsg.result().body())

                eventBus.request<String>(
                  "process.attributes.deleteCustomAttribute",
                  customProductAttribute.attributeKey,
                ).onFailure {
                  testContext.failNow(it)
                }.onComplete { deleteCustomAttributeMsg ->
                  assert(deleteCustomAttributeMsg.succeeded())
                  assertEquals("Custom attribute deleted successfully", deleteCustomAttributeMsg.result().body())

                  eventBus.request<Int>("process.products.deleteProduct", productId).onFailure {
                    testContext.failNow(it)
                  }.onComplete { deleteProductMsg ->
                    assert(deleteProductMsg.succeeded())
                    assertEquals("Product deleted successfully", deleteProductMsg.result().body())

                    testContext.completeNow()
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private fun deployVerticles(vertx: Vertx): MutableList<Future<String>> {
    val verticleList: MutableList<Future<String>> = emptyList<Future<String>>().toMutableList()

    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductCustomAttributesJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )
    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )
    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductMultiSelectJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )
    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductGlobalEavJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )

    return verticleList
  }
}
