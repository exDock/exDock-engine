package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.database.scope.ScopeJdbcVerticle
import com.ex_dock.ex_dock.database.scope.StoreView
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
class ProductStoreViewEavJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private val verticleDeployHelper = VerticleDeployHelper()

  private var productId = -1

  private var storeViewId = -1

  private var websiteId = -1

  private val storeViewBoolDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewBoolCodec")
  private val storeViewFloatDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewFloatCodec")
  private val storeViewIntDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewIntCodec")
  private val storeViewMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewMoneyCodec")
  private val storeViewStringDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewStringCodec")
  private val storeViewMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewMultiSelectCodec")
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val customProductAttributeDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val websitesDeliveryOptions = DeliveryOptions().setCodecName("WebsitesCodec")
  private val storeViewDeliveryOptions = DeliveryOptions().setCodecName("StoreViewCodec")
  private val esvBoolList: MutableList<EavStoreViewBool> = emptyList<EavStoreViewBool>().toMutableList()
  private val esvFloatList: MutableList<EavStoreViewFloat> = emptyList<EavStoreViewFloat>().toMutableList()
  private val esvIntList: MutableList<EavStoreViewInt> = emptyList<EavStoreViewInt>().toMutableList()
  private val esvMoneyList: MutableList<EavStoreViewMoney> = emptyList<EavStoreViewMoney>().toMutableList()
  private val esvStringList: MutableList<EavStoreViewString> = emptyList<EavStoreViewString>().toMutableList()
  private val esvMultiSelectList: MutableList<EavStoreViewMultiSelect> = emptyList<EavStoreViewMultiSelect>().toMutableList()
  private val esvInfoList: MutableList<EavStoreViewInfo> = emptyList<EavStoreViewInfo>().toMutableList()

  private var product = Products(
    productId = productId,
    name = "Test Product",
    shortName = "TProduct",
    description = "Test Product Description",
    shortDescription = "TPDescription"
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

  private var storeView = StoreView(
    storeViewId = storeViewId,
    websiteId = websiteId,
    storeViewName = "Test Store View"
  )


  private lateinit var bool: EavStoreViewBool

  private lateinit var float: EavStoreViewFloat

  private lateinit var string: EavStoreViewString

  private lateinit var int: EavStoreViewInt

  private lateinit var money: EavStoreViewMoney

  private lateinit var multiSelect: EavStoreViewMultiSelect

  private lateinit var eav: Eav

  private lateinit var expectedFullEav: EavStoreViewInfo


  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(Websites::class))
      .registerCodec(GenericCodec(StoreView::class))
      .registerCodec(GenericCodec(CustomProductAttributes::class))
      .registerCodec(GenericCodec(Eav::class))
      .registerCodec(GenericCodec(EavStoreViewBool::class))
      .registerCodec(GenericCodec(EavStoreViewFloat::class))
      .registerCodec(GenericCodec(EavStoreViewString::class))
      .registerCodec(GenericCodec(EavStoreViewInt::class))
      .registerCodec(GenericCodec(EavStoreViewMoney::class))
      .registerCodec(GenericCodec(EavStoreViewInfo::class))
      .registerCodec(GenericCodec(EavStoreViewMultiSelect::class))
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

          storeView.websiteId = websiteId

          eventBus.request<StoreView>("process.scope.createStoreView", storeView, storeViewDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createStoreViewMsg ->
            storeView = createStoreViewMsg.result().body()
            storeViewId = storeView.storeViewId
            assertEquals(storeView, createStoreViewMsg.result().body())

            eventBus.request<CustomProductAttributes>("process.attributes.createCustomAttribute", customProductAttribute, customProductAttributeDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { createAttributeMsg ->
              assert(createAttributeMsg.succeeded())
              assertEquals(customProductAttribute, createAttributeMsg.result().body())
              setAllJsonFields()

              eventBus.request<EavStoreViewBool>("process.eavStoreView.createEavStoreViewBool", bool, storeViewBoolDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { createEavStoreViewBoolMsg ->
                assert(createEavStoreViewBoolMsg.succeeded())
                assertEquals(bool, createEavStoreViewBoolMsg.result().body())

                eventBus.request<EavStoreViewFloat>("process.eavStoreView.createEavStoreViewFloat", float, storeViewFloatDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { createEavStoreViewFloatMsg ->
                  assert(createEavStoreViewFloatMsg.succeeded())
                  assertEquals(float, createEavStoreViewFloatMsg.result().body())

                  eventBus.request<EavStoreViewString>("process.eavStoreView.createEavStoreViewString", string, storeViewStringDeliveryOptions).onFailure {
                    testContext.failNow(it)
                  }.onComplete { createEavStoreViewStringMsg ->
                    assert(createEavStoreViewStringMsg.succeeded())
                    assertEquals(string, createEavStoreViewStringMsg.result().body())

                    eventBus.request<EavStoreViewInt>("process.eavStoreView.createEavStoreViewInt", int, storeViewIntDeliveryOptions).onFailure {
                      testContext.failNow(it)
                    }.onComplete { createEavStoreViewIntMsg ->
                      assert(createEavStoreViewIntMsg.succeeded())
                      assertEquals(int, createEavStoreViewIntMsg.result().body())

                      eventBus.request<EavStoreViewMoney>("process.eavStoreView.createEavStoreViewMoney", money, storeViewMoneyDeliveryOptions).onFailure {
                        testContext.failNow(it)
                      }.onComplete { createEavStoreViewMoneyMsg ->
                        assert(createEavStoreViewMoneyMsg.succeeded())
                        assertEquals(
                          money,
                          createEavStoreViewMoneyMsg.result().body()
                        )

                        eventBus.request<EavStoreViewMultiSelect>("process.eavStoreView.createEavStoreViewMultiSelect", multiSelect, storeViewMultiSelectDeliveryOptions)
                          .onFailure {
                            testContext.failNow(it)
                          }.onComplete { createEavStoreViewEavStoreViewMsg ->
                            assert(createEavStoreViewEavStoreViewMsg.succeeded())
                            assertEquals(
                              multiSelect,
                              createEavStoreViewEavStoreViewMsg.result().body()
                            )

                            eventBus.request<Eav>("process.eavStoreView.createEavStoreView", eav, eavDeliveryOptions).onFailure {
                              testContext.failNow(it)
                            }.onComplete { createEavStoreViewMsg ->
                              assert(createEavStoreViewMsg.succeeded())
                              assertEquals(eav, createEavStoreViewMsg.result().body())

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
  }

  @Test
  fun testGetAllEavStoreViewBool(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewBool>>("process.eavStoreView.getAllEavStoreViewBool", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewBoolMsg ->
      assert(getAllEavStoreViewBoolMsg.succeeded())
      assertEquals(
        esvBoolList,
        getAllEavStoreViewBoolMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewBoolByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewBool>("process.eavStoreView.getEavStoreViewBoolByKey", bool, storeViewBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(bool, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewBool(vertx: Vertx, testContext: VertxTestContext) {
    val updatedBool = EavStoreViewBool(
      productId = productId,
      attributeKey = bool.attributeKey,
      storeViewId = storeViewId,
      value = false
    )

    eventBus.request<EavStoreViewBool>("process.eavStoreView.updateEavStoreViewBool", updatedBool, storeViewBoolDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedBool, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavStoreViewBool>("process.eavStoreView.getEavStoreViewBoolByKey", updatedBool, storeViewBoolDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewBoolMsg ->
        assert(getUpdatedEavStoreViewBoolMsg.succeeded())
        assertEquals(updatedBool, getUpdatedEavStoreViewBoolMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewFloat(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewFloat>>("process.eavStoreView.getAllEavStoreViewFloat", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewFloatMsg ->
      assert(getAllEavStoreViewFloatMsg.succeeded())
      assertEquals(
        esvFloatList, getAllEavStoreViewFloatMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewFloatByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewFloat>("process.eavStoreView.getEavStoreViewFloatByKey", float, storeViewFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(float, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewFloat(vertx: Vertx, testContext: VertxTestContext) {
    val updatedFloat = EavStoreViewFloat(
      productId = productId,
      attributeKey = float.attributeKey,
      storeViewId = storeViewId,
      value = 10.5F
    )

    eventBus.request<EavStoreViewFloat>("process.eavStoreView.updateEavStoreViewFloat", updatedFloat, storeViewFloatDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedFloat, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavStoreViewFloat>("process.eavStoreView.getEavStoreViewFloatByKey", updatedFloat, storeViewFloatDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewFloatMsg ->
        assert(getUpdatedEavStoreViewFloatMsg.succeeded())
        assertEquals(updatedFloat, getUpdatedEavStoreViewFloatMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewString(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewString>>("process.eavStoreView.getAllEavStoreViewString", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewStringMsg ->
      assert(getAllEavStoreViewStringMsg.succeeded())
      assertEquals(
        esvStringList,
        getAllEavStoreViewStringMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewStringByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewString>("process.eavStoreView.getEavStoreViewStringByKey", string, storeViewStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(string, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewString(vertx: Vertx, testContext: VertxTestContext) {
    val updatedString = EavStoreViewString(
      productId = productId,
      attributeKey = string.attributeKey,
      storeViewId = storeViewId,
      value = "New EAV store view string"
    )

    eventBus.request<EavStoreViewString>("process.eavStoreView.updateEavStoreViewString", updatedString, storeViewStringDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedString, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavStoreViewString>("process.eavStoreView.getEavStoreViewStringByKey", updatedString, storeViewStringDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewStringMsg ->
        assert(getUpdatedEavStoreViewStringMsg.succeeded())
        assertEquals(updatedString, getUpdatedEavStoreViewStringMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewInt(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewInt>>("process.eavStoreView.getAllEavStoreViewInt", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewIntMsg ->
      assert(getAllEavStoreViewIntMsg.succeeded())
      assertEquals(
        esvIntList,
        getAllEavStoreViewIntMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewIntByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewInt>("process.eavStoreView.getEavStoreViewIntByKey", int, storeViewIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(int, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewInt(vertx: Vertx, testContext: VertxTestContext) {
    val updatedInt = EavStoreViewInt(
      productId = productId,
      attributeKey = int.attributeKey,
      storeViewId = storeViewId,
      value = 1000
    )

    eventBus.request<EavStoreViewInt>("process.eavStoreView.updateEavStoreViewInt", updatedInt, storeViewIntDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedInt, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavStoreViewInt>("process.eavStoreView.getEavStoreViewIntByKey", updatedInt, storeViewIntDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewIntMsg ->
        assert(getUpdatedEavStoreViewIntMsg.succeeded())
        assertEquals(updatedInt, getUpdatedEavStoreViewIntMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewMoney(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewMoney>>("process.eavStoreView.getAllEavStoreViewMoney", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewMoneyMsg ->
      assert(getAllEavStoreViewMoneyMsg.succeeded())
      assertEquals(
        esvMoneyList,
        getAllEavStoreViewMoneyMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewMoneyByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewMoney>("process.eavStoreView.getEavStoreViewMoneyByKey", money, storeViewMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(money, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewMoney(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMoney = EavStoreViewMoney(
      productId = productId,
      attributeKey = money.attributeKey,
      storeViewId = storeViewId,
      value = 100.50
    )

    eventBus.request<EavStoreViewMoney>("process.eavStoreView.updateEavStoreViewMoney", updatedMoney, storeViewMoneyDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedMoney, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavStoreViewMoney>("process.eavStoreView.getEavStoreViewMoneyByKey", updatedMoney, storeViewMoneyDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewMoneyMsg ->
        assert(getUpdatedEavStoreViewMoneyMsg.succeeded())
        assertEquals(updatedMoney, getUpdatedEavStoreViewMoneyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewMultiSelect>>("process.eavStoreView.getAllEavStoreViewMultiSelect", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewEavStoreViewMsg ->
      assert(getAllEavStoreViewEavStoreViewMsg.succeeded())
      assertEquals(
        esvMultiSelectList,
        getAllEavStoreViewEavStoreViewMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewMultiSelectByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<EavStoreViewMultiSelect>("process.eavStoreView.getEavStoreViewMultiSelectByKey", multiSelect, storeViewMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(multiSelect, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreViewMultiSelect(vertx: Vertx, testContext: VertxTestContext) {
    val updatedMultiSelect = EavStoreViewMultiSelect(
      productId = productId,
      attributeKey = multiSelect.attributeKey,
      storeViewId = storeViewId,
      value = 1
    )

    eventBus.request<EavGlobalMultiSelect>("process.eavStoreView.updateEavStoreViewMultiSelect", updatedMultiSelect, storeViewMultiSelectDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedMultiSelect, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<EavGlobalMultiSelect>("process.eavStoreView.getEavStoreViewMultiSelectByKey", updatedMultiSelect, storeViewMultiSelectDeliveryOptions)
        .onFailure {
          testContext.failNow(it)
        }.onComplete { getUpdatedEavStoreViewEavStoreViewMsg ->
        assert(getUpdatedEavStoreViewEavStoreViewMsg.succeeded())
        assertEquals(updatedMultiSelect, getUpdatedEavStoreViewEavStoreViewMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreView(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<Eav>>("process.eavStoreView.getAllEavStoreView", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewMsg ->
      assert(getAllEavStoreViewMsg.succeeded())
      assertEquals(
        mutableListOf(eav),
        getAllEavStoreViewMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Eav>("process.eavStoreView.getEavStoreViewByKey", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewByKeyMsg ->
      assert(getEavStoreViewByKeyMsg.succeeded())
      assertEquals(eav, getEavStoreViewByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateEavStoreView(vertx: Vertx, testContext: VertxTestContext) {
    val updatedEav = Eav(
      productId = productId,
      attributeKey = eav.attributeKey,
    )

    eventBus.request<Eav>("process.eavStoreView.updateEavStoreView", updatedEav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateEavStoreViewByKeyMsg ->
      assert(updateEavStoreViewByKeyMsg.succeeded())
      assertEquals(updatedEav, updateEavStoreViewByKeyMsg.result().body())

      eventBus.request<Eav>("process.eavStoreView.getEavStoreViewByKey", updatedEav, eavDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedEavStoreViewMsg ->
        assert(getUpdatedEavStoreViewMsg.succeeded())
        assertEquals(updatedEav, getUpdatedEavStoreViewMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllEavStoreViewInfo(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewInfo>>("process.eavStoreView.getAllEavStoreViewInfo", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllEavStoreViewInfoMsg ->
      assert(getAllEavStoreViewInfoMsg.succeeded())
      assertEquals(
        esvInfoList, getAllEavStoreViewInfoMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @Test
  fun testGetEavStoreViewInfoByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<EavStoreViewInfo>>("process.eavStoreView.getEavStoreViewInfoByKey", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getEavStoreViewInfoByKeyMsg ->
      assert(getEavStoreViewInfoByKeyMsg.succeeded())
      assertEquals(
        esvInfoList, getEavStoreViewInfoByKeyMsg.result().body()
      )

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.eavStoreView.deleteEavStoreView", eav, eavDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteEavStoreViewMsg ->
      assert(deleteEavStoreViewMsg.succeeded())
      assertEquals("EAV storeView deleted successfully", deleteEavStoreViewMsg.result().body())

      eventBus.request<String>("process.eavStoreView.deleteEavStoreViewMultiSelect", multiSelect, storeViewMultiSelectDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteEavStoreViewEavStoreViewMsg ->
        assert(deleteEavStoreViewEavStoreViewMsg.succeeded())
        assertEquals(
          "EAV storeView multi-select deleted successfully",
          deleteEavStoreViewEavStoreViewMsg.result().body()
        )

        eventBus.request<String>("process.eavStoreView.deleteEavStoreViewMoney", money, storeViewMoneyDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteEavStoreViewMoneyMsg ->
          assert(deleteEavStoreViewMoneyMsg.succeeded())
          assertEquals("EAV storeView money deleted successfully", deleteEavStoreViewMoneyMsg.result().body())

          eventBus.request<String>("process.eavStoreView.deleteEavStoreViewInt", int, storeViewIntDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { deleteEavStoreViewIntMsg ->
            assert(deleteEavStoreViewIntMsg.succeeded())
            assertEquals("EAV storeView int deleted successfully", deleteEavStoreViewIntMsg.result().body())

            eventBus.request<String>("process.eavStoreView.deleteEavStoreViewString", string, storeViewStringDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { deleteEavStoreViewStringMsg ->
              assert(deleteEavStoreViewStringMsg.succeeded())
              assertEquals("EAV storeView string deleted successfully", deleteEavStoreViewStringMsg.result().body())

              eventBus.request<String>("process.eavStoreView.deleteEavStoreViewFloat", float, storeViewFloatDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { deleteEavStoreViewFloatMsg ->
                assert(deleteEavStoreViewFloatMsg.succeeded())
                assertEquals("EAV storeView float deleted successfully", deleteEavStoreViewFloatMsg.result().body())

                eventBus.request<String>("process.eavStoreView.deleteEavStoreViewBool", bool, storeViewBoolDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { deleteEavStoreViewBoolMsg ->
                  assert(deleteEavStoreViewBoolMsg.succeeded())
                  assertEquals("EAV storeView bool deleted successfully", deleteEavStoreViewBoolMsg.result().body())

                      eventBus.request<String>(
                        "process.attributes.deleteCustomAttribute",
                        customProductAttribute.attributeKey
                      ).onFailure {
                        testContext.failNow(it)
                      }.onComplete { deleteAttributeMsg ->
                        assert(deleteAttributeMsg.succeeded())
                        assertEquals("Custom attribute deleted successfully", deleteAttributeMsg.result().body())

                        eventBus.request<String>("process.scope.deleteStoreView", storeViewId).onFailure {
                          testContext.failNow(it)
                        }.onComplete { deleteStoreViewMsg ->
                          assert(deleteStoreViewMsg.succeeded())
                          assertEquals("Store view deleted successfully", deleteStoreViewMsg.result().body())

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
  }

  private fun setAllJsonFields() {
    bool = EavStoreViewBool(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = true
    )

    float = EavStoreViewFloat(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = 10.0f
    )

    string = EavStoreViewString(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = "test string"
    )

    int = EavStoreViewInt(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = 10
    )

    money = EavStoreViewMoney(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = 10.0
    )

    multiSelect = EavStoreViewMultiSelect(
      productId = productId,
      storeViewId = storeViewId,
      attributeKey = "test attribute key",
      value = 1
    )

    eav = Eav(
      productId = productId,
      attributeKey = "test attribute key",
    )

    expectedFullEav = EavStoreViewInfo(
      product = product,
      attributeKey = "test attribute key",
      eavStoreViewBool = bool.value,
      eavStoreViewFloat = float.value,
      eavStoreViewString = string.value,
      eavStoreViewInt = int.value,
      eavStoreViewMoney = money.value,
      eavStoreViewMultiSelect = multiSelect.value,
    )

    esvBoolList.add(bool)
    esvFloatList.add(float)
    esvStringList.add(string)
    esvIntList.add(int)
    esvMoneyList.add(money)
    esvMultiSelectList.add(multiSelect)
    esvInfoList.add(expectedFullEav)
  }

  private fun deployVerticles(vertx: Vertx): MutableList<Future<Void>> {
    val verticleList: MutableList<Future<Void>> = emptyList<Future<Void>>().toMutableList()

    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        ProductStoreViewEavJdbcVerticle::class.qualifiedName.toString(), 5, 5
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
