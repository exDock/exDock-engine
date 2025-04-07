package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.database.scope.ScopeJdbcVerticle
import com.ex_dock.ex_dock.database.scope.Websites
import com.ex_dock.ex_dock.helper.VerticleDeployHelper
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
class ProductWebsiteEavJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private val verticleDeployHelper = VerticleDeployHelper()

  private var productId = -1

  private var websiteId = -1

  private val websiteBoolDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteBoolCodec")
  private val websiteFloatDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteFloatCodec")
  private val websiteIntDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteIntCodec")
  private val websiteMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteMoneyCodec")
  private val websiteStringDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteStringCodec")
  private val websiteMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteMultiSelectCodec")
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val customProductAttributeDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val websitesDeliveryOptions = DeliveryOptions().setCodecName("WebsitesCodec")
  private val ewBoolList: MutableList<EavWebsiteBool> = emptyList<EavWebsiteBool>().toMutableList()
  private val ewFloatList: MutableList<EavWebsiteFloat> = emptyList<EavWebsiteFloat>().toMutableList()
  private val ewIntList: MutableList<EavWebsiteInt> = emptyList<EavWebsiteInt>().toMutableList()
  private val ewMoneyList: MutableList<EavWebsiteMoney> = emptyList<EavWebsiteMoney>().toMutableList()
  private val ewStringList: MutableList<EavWebsiteString> = emptyList<EavWebsiteString>().toMutableList()
  private val ewMultiSelectList: MutableList<EavWebsiteMultiSelect> = emptyList<EavWebsiteMultiSelect>().toMutableList()
  private val ewInfoList: MutableList<EavWebsiteInfo> = emptyList<EavWebsiteInfo>().toMutableList()
  private val eavList: MutableList<Eav> = emptyList<Eav>().toMutableList()

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

  private var website = Websites(
    websiteId = websiteId,
    websiteName = "Test Website",
  )

  private lateinit var bool: EavWebsiteBool

  private lateinit var float: EavWebsiteFloat

  private lateinit var string: EavWebsiteString

  private lateinit var int: EavWebsiteInt

  private lateinit var money: EavWebsiteMoney

  private lateinit var multiSelect: EavWebsiteMultiSelect

  private lateinit var eav: Eav

  private lateinit var expectedFullEav: EavWebsiteInfo


  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(Websites::class))
      .registerCodec(GenericCodec(CustomProductAttributes::class))
      .registerCodec(GenericCodec(Eav::class))
      .registerCodec(GenericCodec(EavWebsiteBool::class))
      .registerCodec(GenericCodec(EavWebsiteFloat::class))
      .registerCodec(GenericCodec(EavWebsiteString::class))
      .registerCodec(GenericCodec(EavWebsiteInt::class))
      .registerCodec(GenericCodec(EavWebsiteMoney::class))
      .registerCodec(GenericCodec(EavWebsiteInfo::class))
      .registerCodec(GenericCodec(EavWebsiteMultiSelect::class))
    Future.all(deployVerticles(vertx)).onFailure {
      testContext.failNow(it)
    }.onComplete {
      eventBus.request<Products>("process.products.createProduct", product, productDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createProductMsg ->
        product = createProductMsg.result().body()
        productId = product.productId
        assertEquals(product, createProductMsg.result().body())

        eventBus.request<Websites>("process.scope.createWebsite", website, websitesDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createWebsiteMsg ->
          website = createWebsiteMsg.result().body()
          websiteId = website.websiteId!!
          assertEquals(website, createWebsiteMsg.result().body())

            eventBus.request<CustomProductAttributes>("process.attributes.createCustomAttribute", customProductAttribute, customProductAttributeDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { createAttributeMsg ->
              assert(createAttributeMsg.succeeded())
              assertEquals(customProductAttribute, createAttributeMsg.result().body())
              setAllJsonFields()

              eventBus.request<EavWebsiteBool>("process.eavWebsite.createEavWebsiteBool", bool, websiteBoolDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { createEavWebsiteBoolMsg ->
                assert(createEavWebsiteBoolMsg.succeeded())
                assertEquals(bool, createEavWebsiteBoolMsg.result().body())

                eventBus.request<EavWebsiteFloat>("process.eavWebsite.createEavWebsiteFloat", float, websiteFloatDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { createEavWebsiteFloatMsg ->
                  assert(createEavWebsiteFloatMsg.succeeded())
                  assertEquals(float, createEavWebsiteFloatMsg.result().body())

                  eventBus.request<EavWebsiteString>("process.eavWebsite.createEavWebsiteString", string, websiteStringDeliveryOptions).onFailure {
                    testContext.failNow(it)
                  }.onComplete { createEavWebsiteStringMsg ->
                    assert(createEavWebsiteStringMsg.succeeded())
                    assertEquals(string, createEavWebsiteStringMsg.result().body())

                    eventBus.request<EavWebsiteInt>("process.eavWebsite.createEavWebsiteInt", int, websiteIntDeliveryOptions).onFailure {
                      testContext.failNow(it)
                    }.onComplete { createEavWebsiteIntMsg ->
                      assert(createEavWebsiteIntMsg.succeeded())
                      assertEquals(int, createEavWebsiteIntMsg.result().body())

                      eventBus.request<EavWebsiteMoney>("process.eavWebsite.createEavWebsiteMoney", money, websiteMoneyDeliveryOptions).onFailure {
                        testContext.failNow(it)
                      }.onComplete { createEavWebsiteMoneyMsg ->
                        assert(createEavWebsiteMoneyMsg.succeeded())
                        assertEquals(
                          money,
                          createEavWebsiteMoneyMsg.result().body()
                        )

                        eventBus.request<EavWebsiteMultiSelect>("process.eavWebsite.createEavWebsiteMultiSelect", multiSelect, websiteMultiSelectDeliveryOptions)
                          .onFailure {
                            testContext.failNow(it)
                          }.onComplete { createEavWebsiteMultiSelectMsg ->
                            assert(createEavWebsiteMultiSelectMsg.succeeded())
                            assertEquals(
                              multiSelect,
                              createEavWebsiteMultiSelectMsg.result().body()
                            )

                            eventBus.request<Eav>("process.eavWebsite.createEavWebsite", eav, eavDeliveryOptions).onFailure {
                              testContext.failNow(it)
                            }.onComplete { createEavWebsiteMsg ->
                              assert(createEavWebsiteMsg.succeeded())
                              assertEquals(eav, createEavWebsiteMsg.result().body())

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
  }

  @Test
  fun testGetAllEavWebsiteBool(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteBool>>("process.eavWebsite.getAllEavWebsiteBool", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteBoolMsg ->
      assert(getAllEavWebsiteBoolMsg.succeeded())
      assertEquals(
        ewBoolList,
        getAllEavWebsiteBoolMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteBoolByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteBool>("process.eavWebsite.getEavWebsiteBoolByKey", bool, websiteBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(bool, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteBool(vertx: Vertx, testContext: VertxTestContext) {
    val updatedBool = EavWebsiteBool(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = false
    )

    eventBus.request<EavWebsiteBool>("process.eavWebsite.updateEavWebsiteBool", updatedBool, websiteBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedBool, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteBool>("process.eavWebsite.getEavWebsiteBoolByKey", updatedBool, websiteBoolDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteBoolMsg ->
        assert(getUpdatedEavWebsiteBoolMsg.succeeded())
        assertEquals(updatedBool, getUpdatedEavWebsiteBoolMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteFloat(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteFloat>>("process.eavWebsite.getAllEavWebsiteFloat", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteFloatMsg ->
      assert(getAllEavWebsiteFloatMsg.succeeded())
      assertEquals(
        ewFloatList,
        getAllEavWebsiteFloatMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteFloatByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteFloat>("process.eavWebsite.getEavWebsiteFloatByKey", float, websiteFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(float, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteFloat(vertx: Vertx, testContext: VertxTestContext) {
    val updatedFloat = EavWebsiteFloat(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = 100.0f
    )

    eventBus.request<EavWebsiteFloat>("process.eavWebsite.updateEavWebsiteFloat", updatedFloat, websiteFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedFloat, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteFloat>("process.eavWebsite.getEavWebsiteFloatByKey", updatedFloat, websiteFloatDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteFloatMsg ->
        assert(getUpdatedEavWebsiteFloatMsg.succeeded())
        assertEquals(updatedFloat, getUpdatedEavWebsiteFloatMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteString(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteString>>("process.eavWebsite.getAllEavWebsiteString", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteStringMsg ->
      assert(getAllEavWebsiteStringMsg.succeeded())
      assertEquals(
        ewStringList,
        getAllEavWebsiteStringMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteStringByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteString>("process.eavWebsite.getEavWebsiteStringByKey", string, websiteStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(string, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteString(vertx: Vertx, testContext: VertxTestContext) {
    val updatedString = EavWebsiteString(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = "updated string"
    )

    eventBus.request<EavWebsiteString>("process.eavWebsite.updateEavWebsiteString", updatedString, websiteStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedString, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteString>("process.eavWebsite.getEavWebsiteStringByKey", updatedString, websiteStringDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteStringMsg ->
        assert(getUpdatedEavWebsiteStringMsg.succeeded())
        assertEquals(updatedString, getUpdatedEavWebsiteStringMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteInt(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteInt>>("process.eavWebsite.getAllEavWebsiteInt", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteIntMsg ->
      assert(getAllEavWebsiteIntMsg.succeeded())
      assertEquals(
        ewIntList,
        getAllEavWebsiteIntMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteIntByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteInt>("process.eavWebsite.getEavWebsiteIntByKey", int, websiteIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(int, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteInt(vertx: Vertx, testContext: VertxTestContext) {
    val updatedInt = EavWebsiteInt(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = 100
    )

    eventBus.request<EavWebsiteInt>("process.eavWebsite.updateEavWebsiteInt", updatedInt, websiteIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedInt, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteInt>("process.eavWebsite.getEavWebsiteIntByKey", updatedInt, websiteIntDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteIntMsg ->
        assert(getUpdatedEavWebsiteIntMsg.succeeded())
        assertEquals(updatedInt, getUpdatedEavWebsiteIntMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteMoney(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteMoney>>("process.eavWebsite.getAllEavWebsiteMoney", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteMoneyMsg ->
      assert(getAllEavWebsiteMoneyMsg.succeeded())
      assertEquals(
        ewMoneyList,
        getAllEavWebsiteMoneyMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteMoneyByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteMoney>("process.eavWebsite.getEavWebsiteMoneyByKey", money, websiteMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(money, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteMoney(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMoney = EavWebsiteMoney(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = 100.00
    )

    eventBus.request<EavWebsiteMoney>("process.eavWebsite.updateEavWebsiteMoney", updatedMoney, websiteMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedMoney, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteMoney>("process.eavWebsite.getEavWebsiteMoneyByKey", updatedMoney, websiteMoneyDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteMoneyMsg ->
        assert(getUpdatedEavWebsiteMoneyMsg.succeeded())
        assertEquals(updatedMoney, getUpdatedEavWebsiteMoneyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteMultiSelect>>("process.eavWebsite.getAllEavWebsiteMultiSelect", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteMultiSelectMsg ->
      assert(getAllEavWebsiteMultiSelectMsg.succeeded())
      assertEquals(
        ewMultiSelectList,
        getAllEavWebsiteMultiSelectMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteMultiSelectByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavWebsiteMultiSelect>("process.eavWebsite.getEavWebsiteMultiSelectByKey", multiSelect, websiteMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(multiSelect, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsiteMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMultiSelect = EavWebsiteMultiSelect(
      productId = productId,
      websiteId = websiteId,
      attributeKey = customProductAttribute.attributeKey,
      value = 2
    )

    eventBus.request<EavWebsiteMultiSelect>("process.eavWebsite.updateEavWebsiteMultiSelect", updatedMultiSelect, websiteMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedMultiSelect, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<EavWebsiteMultiSelect>("process.eavWebsite.getEavWebsiteMultiSelectByKey", updatedMultiSelect, websiteMultiSelectDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedEavWebsiteMultiSelectMsg ->
          assert(getUpdatedEavWebsiteMultiSelectMsg.succeeded())
          assertEquals(updatedMultiSelect, getUpdatedEavWebsiteMultiSelectMsg.result().body())

          testContext.completeNow()
        }
    }
  }

  @Test
  fun testGetAllEavWebsite(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<Eav>>("process.eavWebsite.getAllEavWebsite", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteMsg ->
      assert(getAllEavWebsiteMsg.succeeded())
      assertEquals(
        eavList,
        getAllEavWebsiteMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Eav>("process.eavWebsite.getEavWebsiteByKey", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteByKeyMsg ->
      assert(getEavWebsiteByKeyMsg.succeeded())
      assertEquals(eav, getEavWebsiteByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavWebsite(vertx: Vertx, testContext: VertxTestContext) {
    val updatedEav = Eav(
      productId = productId,
      attributeKey = customProductAttribute.attributeKey,
    )

    eventBus.request<Eav>("process.eavWebsite.updateEavWebsite", updatedEav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavWebsiteByKeyMsg ->
      assert(updateEavWebsiteByKeyMsg.succeeded())
      assertEquals(updatedEav, updateEavWebsiteByKeyMsg.result().body())

      eventBus.request<Eav>("process.eavWebsite.getEavWebsiteByKey", updatedEav, eavDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavWebsiteMsg ->
        assert(getUpdatedEavWebsiteMsg.succeeded())
        assertEquals(updatedEav, getUpdatedEavWebsiteMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavWebsiteInfo(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteInfo>>("process.eavWebsite.getAllEavWebsiteInfo", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavWebsiteInfoMsg ->
      assert(getAllEavWebsiteInfoMsg.succeeded())
      assertEquals(
        ewInfoList, getAllEavWebsiteInfoMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavWebsiteInfoByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavWebsiteInfo>>("process.eavWebsite.getEavWebsiteInfoByKey", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavWebsiteInfoByKeyMsg ->
      assert(getEavWebsiteInfoByKeyMsg.succeeded())
      assertEquals(
        ewInfoList, getEavWebsiteInfoByKeyMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.eavWebsite.deleteEavWebsite", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteEavWebsiteMsg ->
      assert(deleteEavWebsiteMsg.succeeded())
      assertEquals("EAV website deleted successfully", deleteEavWebsiteMsg.result().body())

      eventBus.request<String>("process.eavWebsite.deleteEavWebsiteMultiSelect", multiSelect, websiteMultiSelectDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteEavWebsiteMultiSelectMsg ->
        assert(deleteEavWebsiteMultiSelectMsg.succeeded())
        assertEquals(
          "EAV website multi-select deleted successfully",
          deleteEavWebsiteMultiSelectMsg.result().body()
        )

        eventBus.request<String>("process.eavWebsite.deleteEavWebsiteMoney", money, websiteMoneyDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteEavWebsiteMoneyMsg ->
          assert(deleteEavWebsiteMoneyMsg.succeeded())
          assertEquals("EAV website money deleted successfully", deleteEavWebsiteMoneyMsg.result().body())

          eventBus.request<String>("process.eavWebsite.deleteEavWebsiteInt", int, websiteIntDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { deleteEavWebsiteIntMsg ->
            assert(deleteEavWebsiteIntMsg.succeeded())
            assertEquals("EAV website int deleted successfully", deleteEavWebsiteIntMsg.result().body())

            eventBus.request<String>("process.eavWebsite.deleteEavWebsiteString", string, websiteStringDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { deleteEavWebsiteStringMsg ->
              assert(deleteEavWebsiteStringMsg.succeeded())
              assertEquals("EAV website string deleted successfully", deleteEavWebsiteStringMsg.result().body())

              eventBus.request<String>("process.eavWebsite.deleteEavWebsiteFloat", float, websiteFloatDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { deleteEavWebsiteFloatMsg ->
                assert(deleteEavWebsiteFloatMsg.succeeded())
                assertEquals("EAV website float deleted successfully", deleteEavWebsiteFloatMsg.result().body())

                eventBus.request<String>("process.eavWebsite.deleteEavWebsiteBool", bool, websiteBoolDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { deleteEavWebsiteBoolMsg ->
                  assert(deleteEavWebsiteBoolMsg.succeeded())
                  assertEquals("EAV website bool deleted successfully", deleteEavWebsiteBoolMsg.result().body())

                  eventBus.request<String>(
                    "process.attributes.deleteCustomAttribute",
                    customProductAttribute.attributeKey
                  ).onFailure {
                    testContext.failNow(it)
                  }.onComplete { deleteAttributeMsg ->
                    assert(deleteAttributeMsg.succeeded())
                    assertEquals("Custom attribute deleted successfully", deleteAttributeMsg.result().body())

                      eventBus.request<String>("process.scope.deleteWebsite", websiteId).onFailure {
                        testContext.failNow(it)
                      }.onComplete { deleteWebsiteMsg ->
                        assert(deleteWebsiteMsg.succeeded())
                        assertEquals("Website deleted successfully", deleteWebsiteMsg.result().body())

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
  }

  private fun setAllJsonFields() {
    bool = EavWebsiteBool(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = true
    )

    float = EavWebsiteFloat(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = 10.0f
    )

    string = EavWebsiteString(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = "test string"
    )

    int = EavWebsiteInt(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = 10
    )

    money = EavWebsiteMoney(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = 10.0
    )

    multiSelect = EavWebsiteMultiSelect(
      productId = productId,
      websiteId = websiteId,
      attributeKey = "test attribute key",
      value = 1
    )

    eav = Eav(
      productId = productId,
      attributeKey = "test attribute key",
    )

    expectedFullEav = EavWebsiteInfo(
      products = product,
      attributeKey = eav.attributeKey,
      eavWebsiteBool = bool.value,
      eavWebsiteFloat = float.value,
      eavWebsiteInt = int.value,
      eavWebsiteMoney = money.value,
      eavWebsiteMultiSelect = multiSelect.value,
      eavWebsiteString = string.value,
    )

    ewBoolList.add(bool)
    ewFloatList.add(float)
    ewIntList.add(int)
    ewMoneyList.add(money)
    ewMultiSelectList.add(multiSelect)
    ewStringList.add(string)
    ewInfoList.add(expectedFullEav)
    eavList.add(eav)
  }

  private fun deployVerticles(vertx: Vertx): MutableList<Future<Void>> {
    val verticleList: MutableList<Future<Void>> = emptyList<Future<Void>>().toMutableList()

    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductWebsiteEavJdbcVerticle::class.qualifiedName.toString(), 5, 5
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
    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ScopeJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )

    return verticleList
  }
}
