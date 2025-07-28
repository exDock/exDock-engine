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
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ProductGlobalEavJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val eavGlobalBoolDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalBoolCodec")
  private val eavGlobalFloatDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalFloatCodec")
  private val eavGlobalIntDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalIntCodec")
  private val eavGlobalMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalMoneyCodec")
  private val eavGlobalMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalMultiSelectCodec")
  private val eavGlobalStringDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalStringCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val customProductAttributeDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private var productId = -1
  private val boolList = emptyList<EavGlobalBool>().toMutableList()
  private val floatList = emptyList<EavGlobalFloat>().toMutableList()
  private val intList = emptyList<EavGlobalInt>().toMutableList()
  private val moneyList = emptyList<EavGlobalMoney>().toMutableList()
  private val multiSelectList = emptyList<EavGlobalMultiSelect>().toMutableList()
  private val stringList = emptyList<EavGlobalString>().toMutableList()
  private val eavList = emptyList<Eav>().toMutableList()
  private val eavGlobalInfoList = emptyList<EavGlobalInfo>().toMutableList()

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

  private lateinit var bool: EavGlobalBool
  private lateinit var float: EavGlobalFloat
  private lateinit var string: EavGlobalString
  private lateinit var int: EavGlobalInt
  private lateinit var money: EavGlobalMoney
  private lateinit var multiSelect: EavGlobalMultiSelect
  private lateinit var eav: Eav
  private lateinit var expectedFullEav: EavGlobalInfo


  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(CustomProductAttributes::class))
      .registerCodec(GenericCodec(EavGlobalBool::class))
      .registerCodec(GenericCodec(EavGlobalFloat::class))
      .registerCodec(GenericCodec(EavGlobalInt::class))
      .registerCodec(GenericCodec(EavGlobalMoney::class))
      .registerCodec(GenericCodec(EavGlobalMultiSelect::class))
      .registerCodec(GenericCodec(EavGlobalString::class))
      .registerCodec(GenericCodec(Eav::class))
      .registerCodec(GenericCodec(EavGlobalInfo::class))
      .registerCodec(GenericCodec(MutableList::class))
    Future.all<String>(deployVerticles(vertx)).onFailure{
      testContext.failNow(it)
    }.onComplete {
      eventBus.request<Products>("process.products.createProduct", product, productDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createProductMsg ->
        product = createProductMsg.result().body()
        productId = product.productId
        assertEquals(product, createProductMsg.result().body())

        eventBus.request<CustomProductAttributes>("process.attributes.createCustomAttribute", customProductAttribute, customProductAttributeDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createAttributeMsg ->
          assert(createAttributeMsg.succeeded())
          setAllJsonFields()
          assertEquals(customProductAttribute, createAttributeMsg.result().body())

          eventBus.request<EavGlobalBool>("process.eavGlobal.createEavGlobalBool", bool, eavGlobalBoolDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createEavGlobalBoolMsg ->
            assert(createEavGlobalBoolMsg.succeeded())
            assertEquals(bool, createEavGlobalBoolMsg.result().body())

            eventBus.request<EavGlobalFloat>("process.eavGlobal.createEavGlobalFloat", float, eavGlobalFloatDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { createEavGlobalFloatMsg ->
              assert(createEavGlobalFloatMsg.succeeded())
              assertEquals(float, createEavGlobalFloatMsg.result().body())

              eventBus.request<EavGlobalString>("process.eavGlobal.createEavGlobalString", string, eavGlobalStringDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { createEavGlobalStringMsg ->
                assert(createEavGlobalStringMsg.succeeded())
                assertEquals(string, createEavGlobalStringMsg.result().body())

                eventBus.request<EavGlobalInt>("process.eavGlobal.createEavGlobalInt", int, eavGlobalIntDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { createEavGlobalIntMsg ->
                  assert(createEavGlobalIntMsg.succeeded())
                  assertEquals(int, createEavGlobalIntMsg.result().body())

                  eventBus.request<EavGlobalMoney>("process.eavGlobal.createEavGlobalMoney", money, eavGlobalMoneyDeliveryOptions).onFailure {
                    testContext.failNow(it)
                  }.onComplete { createEavGlobalMoneyMsg ->
                    assert(createEavGlobalMoneyMsg.succeeded())
                    assertEquals(money, createEavGlobalMoneyMsg.result().body())

                    eventBus.request<EavGlobalMultiSelect>("process.eavGlobal.createEavGlobalMultiSelect", multiSelect, eavGlobalMultiSelectDeliveryOptions)
                      .onFailure {
                        testContext.failNow(it)
                      }.onComplete { createEavGlobalMultiSelectMsg ->
                        assert(createEavGlobalMultiSelectMsg.succeeded())
                        assertEquals(
                          multiSelect,
                          createEavGlobalMultiSelectMsg.result().body()
                        )

                        eventBus.request<Eav>("process.eavGlobal.createEavGlobal", eav, eavDeliveryOptions).onFailure {
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
  }

  @Test
  fun testGetAllEavGlobalBool(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalBool>>("process.eavGlobal.getAllEavGlobalBool", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalBoolMsg ->
      assert(getAllEavGlobalBoolMsg.succeeded())
      assertEquals(boolList, getAllEavGlobalBoolMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalBoolByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalBool>("process.eavGlobal.getEavGlobalBoolByKey", bool, eavGlobalBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(bool, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalBool(vertx: Vertx, testContext: VertxTestContext) {
    val updatedBool = EavGlobalBool(
      productId = productId,
      attributeKey = bool.attributeKey,
      value = false
    )

    eventBus.request<EavGlobalBool>("process.eavGlobal.updateEavGlobalBool", updatedBool, eavGlobalBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedBool, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalBool>("process.eavGlobal.getEavGlobalBoolByKey", updatedBool, eavGlobalBoolDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalBoolMsg ->
        assert(getUpdatedEavGlobalBoolMsg.succeeded())
        assertEquals(updatedBool, getUpdatedEavGlobalBoolMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalFloat(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalFloat>>("process.eavGlobal.getAllEavGlobalFloat", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalFloatMsg ->
      assert(getAllEavGlobalFloatMsg.succeeded())
      assertEquals(floatList, getAllEavGlobalFloatMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalFloatByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalFloat>("process.eavGlobal.getEavGlobalFloatByKey", float, eavGlobalFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(float, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalFloat(vertx: Vertx, testContext: VertxTestContext) {
    val updatedFloat = EavGlobalFloat(
      productId = productId,
      attributeKey = float.attributeKey,
      value = 0.0f
    )

    eventBus.request<EavGlobalFloat>("process.eavGlobal.updateEavGlobalFloat", updatedFloat, eavGlobalFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedFloat, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalFloat>("process.eavGlobal.getEavGlobalFloatByKey", updatedFloat, eavGlobalFloatDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalFloatMsg ->
        assert(getUpdatedEavGlobalFloatMsg.succeeded())
        assertEquals(updatedFloat, getUpdatedEavGlobalFloatMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalString(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalString>>("process.eavGlobal.getAllEavGlobalString", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalStringMsg ->
      assert(getAllEavGlobalStringMsg.succeeded())
      assertEquals(stringList, getAllEavGlobalStringMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalStringByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalString>("process.eavGlobal.getEavGlobalStringByKey", string, eavGlobalStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(string, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalString(vertx: Vertx, testContext: VertxTestContext) {
    val updatedString = EavGlobalString(
      productId = productId,
      attributeKey = string.attributeKey,
      value = "EAV global string updated"
    )

    eventBus.request<EavGlobalString>("process.eavGlobal.updateEavGlobalString", updatedString, eavGlobalStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedString, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalString>("process.eavGlobal.getEavGlobalStringByKey", updatedString, eavGlobalStringDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalStringMsg ->
        assert(getUpdatedEavGlobalStringMsg.succeeded())
        assertEquals(updatedString, getUpdatedEavGlobalStringMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalInt(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalInt>>("process.eavGlobal.getAllEavGlobalInt", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalIntMsg ->
      assert(getAllEavGlobalIntMsg.succeeded())
      assertEquals(intList, getAllEavGlobalIntMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalIntByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalInt>("process.eavGlobal.getEavGlobalIntByKey", int, eavGlobalIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(int, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalInt(vertx: Vertx, testContext: VertxTestContext) {
    val updatedInt = EavGlobalInt(
      productId = productId,
      attributeKey = int.attributeKey,
      value = 100
    )

    eventBus.request<EavGlobalInt>("process.eavGlobal.updateEavGlobalInt", updatedInt, eavGlobalIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedInt, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalInt>("process.eavGlobal.getEavGlobalIntByKey", updatedInt, eavGlobalIntDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalIntMsg ->
        assert(getUpdatedEavGlobalIntMsg.succeeded())
        assertEquals(updatedInt, getUpdatedEavGlobalIntMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalMoney(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalMoney>>("process.eavGlobal.getAllEavGlobalMoney", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalMoneyMsg ->
      assert(getAllEavGlobalMoneyMsg.succeeded())
      assertEquals(moneyList, getAllEavGlobalMoneyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalMoneyByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalMoney>("process.eavGlobal.getEavGlobalMoneyByKey", money, eavGlobalMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(money, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalMoney(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMoney = EavGlobalMoney(
      productId = productId,
      attributeKey = money.attributeKey,
      value = 100.00
    )

    eventBus.request<EavGlobalMoney>("process.eavGlobal.updateEavGlobalMoney", updatedMoney, eavGlobalMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedMoney, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalMoney>("process.eavGlobal.getEavGlobalMoneyByKey", updatedMoney, eavGlobalMoneyDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalMoneyMsg ->
        assert(getUpdatedEavGlobalMoneyMsg.succeeded())
        assertEquals(updatedMoney, getUpdatedEavGlobalMoneyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalMultiSelect>>("process.eavGlobal.getAllEavGlobalMultiSelect", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalMultiSelectMsg ->
      assert(getAllEavGlobalMultiSelectMsg.succeeded())
      assertEquals(multiSelectList, getAllEavGlobalMultiSelectMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalMultiSelectByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavGlobalMultiSelect>("process.eavGlobal.getEavGlobalMultiSelectByKey", multiSelect, eavGlobalMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(multiSelect, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobalMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMultiSelect= EavGlobalMultiSelect(
      productId = productId,
      attributeKey = multiSelect.attributeKey,
      value = 5
    )

    eventBus.request<EavGlobalMultiSelect>("process.eavGlobal.updateEavGlobalMultiSelect", updatedMultiSelect, eavGlobalMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedMultiSelect, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<EavGlobalMultiSelect>("process.eavGlobal.getEavGlobalMultiSelectByKey", updatedMultiSelect, eavGlobalMultiSelectDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalMultiSelectMsg ->
        assert(getUpdatedEavGlobalMultiSelectMsg.succeeded())
        assertEquals(updatedMultiSelect, getUpdatedEavGlobalMultiSelectMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobal(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<Eav>>("process.eavGlobal.getAllEavGlobal", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavGlobalMsg ->
      assert(getAllEavGlobalMsg.succeeded())
      assertEquals(eavList, getAllEavGlobalMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavGlobalByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Eav>("process.eavGlobal.getEavGlobalByKey", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalByKeyMsg ->
      assert(getEavGlobalByKeyMsg.succeeded())
      assertEquals(eav, getEavGlobalByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavGlobal(vertx: Vertx, testContext: VertxTestContext) {
    val updatedEav = Eav(
      productId = productId,
      attributeKey = eav.attributeKey,
    )

    eventBus.request<Eav>("process.eavGlobal.updateEavGlobal", updatedEav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavGlobalByKeyMsg ->
      assert(updateEavGlobalByKeyMsg.succeeded())
      assertEquals(updatedEav, updateEavGlobalByKeyMsg.result().body())

      eventBus.request<Eav>("process.eavGlobal.getEavGlobalByKey", updatedEav, eavDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavGlobalMsg ->
        assert(getUpdatedEavGlobalMsg.succeeded())
        assertEquals(updatedEav, getUpdatedEavGlobalMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavGlobalInfo(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<MutableList<EavGlobalInfo>>("process.eavGlobal.getAllEavGlobalInfo", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllEavGlobalInfoMsg ->
//      assert(getAllEavGlobalInfoMsg.succeeded())
//      assertEquals(eavGlobalInfoList, getAllEavGlobalInfoMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetEavGlobalInfoByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavGlobalInfo>>("process.eavGlobal.getEavGlobalInfoByKey", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavGlobalInfoByKeyMsg ->
      assert(getEavGlobalInfoByKeyMsg.succeeded())
      assertEquals(eavGlobalInfoList, getEavGlobalInfoByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.eavGlobal.deleteEavGlobal", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete {deleteEavGlobalMsg ->
      assert(deleteEavGlobalMsg.succeeded())
      assertEquals("EAV global deleted successfully", deleteEavGlobalMsg.result().body())

      eventBus.request<String>("process.eavGlobal.deleteEavGlobalMultiSelect", multiSelect, eavGlobalMultiSelectDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteEavGlobalMultiSelectMsg ->
        assert(deleteEavGlobalMultiSelectMsg.succeeded())
        assertEquals(
          "EAV global multi-select deleted successfully",
          deleteEavGlobalMultiSelectMsg.result().body()
        )

        eventBus.request<String>("process.eavGlobal.deleteEavGlobalMoney", money, eavGlobalMoneyDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteEavGlobalMoneyMsg ->
          assert(deleteEavGlobalMoneyMsg.succeeded())
          assertEquals("EAV global money deleted successfully", deleteEavGlobalMoneyMsg.result().body())

          eventBus.request<String>("process.eavGlobal.deleteEavGlobalInt", int, eavGlobalIntDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { deleteEavGlobalIntMsg ->
            assert(deleteEavGlobalIntMsg.succeeded())
            assertEquals("EAV global int deleted successfully", deleteEavGlobalIntMsg.result().body())

            eventBus.request<String>("process.eavGlobal.deleteEavGlobalString", string, eavGlobalStringDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { deleteEavGlobalStringMsg ->
              assert(deleteEavGlobalStringMsg.succeeded())
              assertEquals("EAV global string deleted successfully", deleteEavGlobalStringMsg.result().body())

              eventBus.request<String>("process.eavGlobal.deleteEavGlobalFloat", float, eavGlobalFloatDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { deleteEavGlobalFloatMsg ->
                assert(deleteEavGlobalFloatMsg.succeeded())
                assertEquals("EAV global float deleted successfully", deleteEavGlobalFloatMsg.result().body())

                eventBus.request<String>("process.eavGlobal.deleteEavGlobalBool", bool, eavGlobalBoolDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { deleteEavGlobalBoolMsg ->
                  assert(deleteEavGlobalBoolMsg.succeeded())
                  assertEquals("EAV global bool deleted successfully", deleteEavGlobalBoolMsg.result().body())

                  eventBus.request<String>(
                    "process.attributes.deleteCustomAttribute",
                    customProductAttribute.attributeKey
                  ).onFailure {
                    testContext.failNow(it)
                  }.onComplete { deleteAttributeMsg ->
                    assert(deleteAttributeMsg.succeeded())
                    assertEquals("Custom attribute deleted successfully", deleteAttributeMsg.result().body())

                    eventBus.request<String>("process.products.deleteProduct", productId).onFailure {
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
  }

  private fun setAllJsonFields() {
    product.productId = productId

    bool = EavGlobalBool(
      productId = productId,
      attributeKey = "test attribute key",
      value = true
    )

    float = EavGlobalFloat(
      productId = productId,
      attributeKey = "test attribute key",
      value = 1.0f
    )

    string = EavGlobalString(
      productId = productId,
      attributeKey = "test attribute key",
      value = "test value"
    )

    int = EavGlobalInt(
      productId = productId,
      attributeKey = "test attribute key",
      value = 1
    )

    money = EavGlobalMoney(
      productId = productId,
      attributeKey = "test attribute key",
      value = 10.0
    )

    multiSelect = EavGlobalMultiSelect(
      productId = productId,
      attributeKey = "test attribute key",
      value = 1
    )

    eav = Eav(
      productId = productId,
      attributeKey = "test attribute key",
    )

    expectedFullEav = EavGlobalInfo(
      eav = eav,
      eavGlobalBool = bool.value,
      eavGlobalFloat = float.value,
      eavGlobalString = string.value,
      eavGlobalInt = int.value,
      eavGlobalMoney = money.value,
      eavGlobalMultiSelect = multiSelect.value
    )

    boolList.add(bool)
    floatList.add(float)
    stringList.add(string)
    intList.add(int)
    moneyList.add(money)
    multiSelectList.add(multiSelect)
    eavList.add(eav)
    eavGlobalInfoList.add(expectedFullEav)
  }

  private fun deployVerticles(vertx: Vertx): MutableList<Future<String>> {
    val verticleList: MutableList<Future<String>> = emptyList<Future<String>>().toMutableList()

    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductGlobalEavJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )
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

    return verticleList
  }
}
