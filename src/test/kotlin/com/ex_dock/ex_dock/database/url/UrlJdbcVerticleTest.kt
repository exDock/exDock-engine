package com.ex_dock.ex_dock.database.url

import com.ex_dock.ex_dock.database.category.Categories
import com.ex_dock.ex_dock.database.category.CategoryJdbcVerticle
import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.database.product.ProductJdbcVerticle
import com.ex_dock.ex_dock.database.product.Products
import com.ex_dock.ex_dock.database.text_pages.TextPages
import com.ex_dock.ex_dock.database.text_pages.TextPagesJdbcVerticle
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
class UrlJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private var productId = -1

  private var textPageId = -1

  private var categoryId = -1
  private val urlKeysDeliveryOptions = DeliveryOptions().setCodecName("UrlKeysCodec")
  private val textPageUrlsDeliveryOptions = DeliveryOptions().setCodecName("TextPageUrlsCodec")
  private val categoryUrlsDeliveryOptions = DeliveryOptions().setCodecName("CategoryUrlsCodec")
  private val productUrlsDeliveryOptions = DeliveryOptions().setCodecName("ProductUrlsCodec")
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val categoryDeliveryOptions = DeliveryOptions().setCodecName("CategoriesCodec")
  private val textPageDeliveryOptions = DeliveryOptions().setCodecName("TextPagesCodec")
  private val fullUrlRequestInfoDeliveryOptions = DeliveryOptions().setCodecName("FullUrlRequestInfoCodec")

  private var url = UrlKeys(
    urlKey = "/",
    upperKey = "/",
    pageType = convertStringToPageType("product")
  )

  private var textPage = TextPages(
    textPagesId = textPageId,
    name = "test name",
    shortText = "test short_text",
    text = "test text"
  )

  private var category = Categories(
    categoryId = categoryId,
    upperCategory = null,
    name = "test name",
    shortDescription = "test description",
    description = "test description"
  )

  private var product = Products(
    productId = productId,
    name = "test name",
    shortName = "test name",
    description = "test description",
    shortDescription = "test description",
    sku = "sku",
    ean = "ean",
    manufacturer = "manufacturer",
  )

  private var textPageUrl = TextPageUrls(
    urlKeys = "/",
    upperKey = "/",
    textPagesId = textPageId,
  )

  private var productUrl = ProductUrls(
    urlKeys = "/",
    upperKey = "/",
    productId = productId,
  )

  private var categoryUrl = CategoryUrls(
    urlKeys = "/",
    upperKey = "/",
    categoryId = categoryId,
  )

  private var fullUrl = FullUrlKeys(
    urlKeys = url,
    textPage = textPage,
    category = category,
    product = product
  )

  private var joinList = JoinList(
    joinTextPage = true,
    joinCategory = true,
    joinProduct = true
  )

  private var fullUrlRequestInfo = FullUrlRequestInfo(
    urlKeys = url.urlKey,
    upperKey = url.upperKey,
    joinList = joinList
  )

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(UrlKeys::class))
      .registerCodec(GenericCodec(TextPages::class))
      .registerCodec(GenericCodec(Categories::class))
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(TextPageUrls::class))
      .registerCodec(GenericCodec(ProductUrls::class))
      .registerCodec(GenericCodec(CategoryUrls::class))
      .registerCodec(GenericCodec(FullUrlKeys::class))
      .registerCodec(GenericCodec(FullUrlRequestInfo::class))
      .registerCodec(GenericCodec(JoinList::class))
    Future.all(deployNeededVerticles(vertx)).onComplete {
      eventBus.request<UrlKeys>("process.url.createUrlKey", url, urlKeysDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createUrlMsg ->
        assertEquals(url, createUrlMsg.result().body())

        eventBus.request<TextPages>("process.textPages.create", textPage, textPageDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createTextPageMsg ->
          textPage = createTextPageMsg.result().body()
          textPageId = textPage.textPagesId
          assertEquals(textPage, createTextPageMsg.result().body())

          eventBus.request<Categories>("process.categories.create", category, categoryDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createCategoryMsg ->
            category = createCategoryMsg.result().body()
            categoryId = category.categoryId!!
            assertEquals(category, createCategoryMsg.result().body())

            eventBus.request<Products>("process.products.createProduct", product, productDeliveryOptions).onFailure {
              testContext.failNow(it)
            }.onComplete { createProductMsg ->
              product = createProductMsg.result().body()
              productId = product.productId
              assertEquals(product, createProductMsg.result().body())

              setAllJsons()

              eventBus.request<TextPageUrls>("process.url.createTextPageUrl", textPageUrl, textPageUrlsDeliveryOptions).onFailure {
                testContext.failNow(it)
              }.onComplete { createTextPageUrlMsg ->
                assertEquals(textPageUrl, createTextPageUrlMsg.result().body())

                eventBus.request<ProductUrls>("process.url.createProductUrl", productUrl, productUrlsDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete {  createProductUrlMsg ->
                  assertEquals(productUrl, createProductUrlMsg.result().body())

                  eventBus.request<CategoryUrls>("process.url.createCategoryUrl", categoryUrl, categoryUrlsDeliveryOptions).onFailure {
                    testContext.failNow(it)
                  }.onComplete { createCategoryUrlMsg ->
                    assertEquals(categoryUrl, createCategoryUrlMsg.result().body())

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
  @Test
  fun testGetAllUrlKeys(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<UrlKeys>>("process.url.getAllUrlKeys", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllUrlKeysMsg ->
      assert(getAllUrlKeysMsg.succeeded())
      assertEquals(mutableListOf(url), getAllUrlKeysMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetUrlKeyByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<UrlKeys>("process.url.getUrlByKey", url, urlKeysDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getUrlKeyByIdMsg ->
      assert(getUrlKeyByIdMsg.succeeded())
      assertEquals(url, getUrlKeyByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun updateUrlKey(vertx: Vertx, testContext: VertxTestContext) {
    val updateUrl = UrlKeys(
      urlKey = "/",
      upperKey = "/",
      pageType = convertStringToPageType("category")
    )

    eventBus.request<UrlKeys>("process.url.updateUrlKey", updateUrl, urlKeysDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateUrlKeyMsg ->
      assert(updateUrlKeyMsg.succeeded())
      assertEquals(updateUrl, updateUrlKeyMsg.result().body())

      eventBus.request<UrlKeys>("process.url.getUrlByKey", updateUrl, urlKeysDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedUrlByKeyMsg ->
        assert(getUpdatedUrlByKeyMsg.succeeded())
        assertEquals(updateUrl, getUpdatedUrlByKeyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllTextPageUrls(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<TextPageUrls>>("process.url.getAllTextPageUrls", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllTextPageUrlsMsg ->
      assert(getAllTextPageUrlsMsg.succeeded())
      assertEquals(mutableListOf(textPageUrl), getAllTextPageUrlsMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun getTextPageUrlByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<TextPageUrls>("process.url.getTextPageUrlByKey", textPageUrl, textPageUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getTextPageUrlByKeyMsg ->
      assert(getTextPageUrlByKeyMsg.succeeded())
      assertEquals(textPageUrl, getTextPageUrlByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun updateTextPageUrl(vertx: Vertx, testContext: VertxTestContext) {
    val updateTextPageUrl = TextPageUrls(
      textPagesId = textPageId,
      urlKeys = "/",
      upperKey = "/",
    )

    eventBus.request<TextPageUrls>("process.url.updateTextPageUrl", updateTextPageUrl, textPageUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateTextPageUrlMsg ->
      assert(updateTextPageUrlMsg.succeeded())
      assertEquals(updateTextPageUrl, updateTextPageUrlMsg.result().body())

      eventBus.request<TextPageUrls>("process.url.getTextPageUrlByKey", updateTextPageUrl, textPageDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedTextPageUrlByKeyMsg ->
        assert(getUpdatedTextPageUrlByKeyMsg.succeeded())
        assertEquals(updateTextPageUrl, getUpdatedTextPageUrlByKeyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllProductUrls(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<ProductUrls>>("process.url.getAllProductUrls", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllProductUrlsMsg ->
      assert(getAllProductUrlsMsg.succeeded())
      assertEquals(mutableListOf(productUrl), getAllProductUrlsMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun getProductUrlByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<ProductUrls>("process.url.getProductUrlByKey", productUrl, productUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getProductUrlByKeyMsg ->
      assert(getProductUrlByKeyMsg.succeeded())
      assertEquals(productUrl, getProductUrlByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun updateProductUrl(vertx: Vertx, testContext: VertxTestContext) {
    val updateProductUrl = ProductUrls(
      productId = productId,
      urlKeys = "/",
      upperKey = "/",
    )

    eventBus.request<ProductUrls>("process.url.updateProductUrl", updateProductUrl, productUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateProductUrlMsg ->
      assert(updateProductUrlMsg.succeeded())
      assertEquals(updateProductUrl, updateProductUrlMsg.result().body())

      eventBus.request<ProductUrls>("process.url.getProductUrlByKey", updateProductUrl, productUrlsDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedProductUrlByKeyMsg ->
        assert(getUpdatedProductUrlByKeyMsg.succeeded())
        assertEquals(updateProductUrl, getUpdatedProductUrlByKeyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllCategoryUrls(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<CategoryUrls>>("process.url.getAllCategoryUrls", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllCategoryUrlsMsg ->
      assert(getAllCategoryUrlsMsg.succeeded())
      assertEquals(mutableListOf(categoryUrl), getAllCategoryUrlsMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun getCategoryUrlByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<CategoryUrls>("process.url.getCategoryUrlByKey", categoryUrl, categoryUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getCategoryUrlByKeyMsg ->
      assert(getCategoryUrlByKeyMsg.succeeded())
      assertEquals(categoryUrl, getCategoryUrlByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun updateCategoryUrl(vertx: Vertx, testContext: VertxTestContext) {
    val updateCategoryUrl = CategoryUrls(
      categoryId = categoryId,
      urlKeys = "/",
      upperKey = "/",
    )

    eventBus.request<CategoryUrls>("process.url.updateCategoryUrl", updateCategoryUrl, categoryUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateCategoryUrlMsg ->
      assert(updateCategoryUrlMsg.succeeded())
      assertEquals(updateCategoryUrl, updateCategoryUrlMsg.result().body())

      eventBus.request<CategoryUrls>("process.url.getCategoryUrlByKey", updateCategoryUrl, categoryUrlsDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedCategoryUrlByKeyMsg ->
        assert(getUpdatedCategoryUrlByKeyMsg.succeeded())
        assertEquals(updateCategoryUrl, getUpdatedCategoryUrlByKeyMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllFullUrlsFullJoin(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<FullUrlKeys>>("process.url.getAllFullUrls", fullUrlRequestInfo, fullUrlRequestInfoDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete {  getAllFullJoinMsg ->
      assert(getAllFullJoinMsg.succeeded())
      assertEquals(mutableListOf(fullUrl), getAllFullJoinMsg.result().body())

      testContext.completeNow()

    }
  }

  @Test
  fun testGetFullUrlByKey(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<FullUrlKeys>("process.url.getFullUrlByKey", fullUrlRequestInfo, fullUrlRequestInfoDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { getFullUrlByKeyMsg ->
      assert(getFullUrlByKeyMsg.succeeded())
      assertEquals(fullUrl, getFullUrlByKeyMsg.result().body())

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.url.deleteCategoryUrl", categoryUrl, categoryUrlsDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteCategoryUrlMsg ->
      assert(deleteCategoryUrlMsg.succeeded())
      assertEquals("Category url deleted successfully", deleteCategoryUrlMsg.result().body())

      eventBus.request<String>("process.url.deleteProductUrl", productUrl, productUrlsDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteProductUrlMsg ->
        assert(deleteProductUrlMsg.succeeded())
        assertEquals("Product url deleted successfully", deleteProductUrlMsg.result().body())

        eventBus.request<String>("process.url.deleteTextPageUrl", textPageUrl, textPageDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteTextPageUrlMsg ->
          assert(deleteTextPageUrlMsg.succeeded())
          assertEquals("Text page url deleted successfully", deleteTextPageUrlMsg.result().body())

          eventBus.request<String>("process.products.deleteProduct", productId).onFailure {
            testContext.failNow(it)
          }.onComplete { deleteProductMsg ->
            assert(deleteProductMsg.succeeded())
            assertEquals("Product deleted successfully", deleteProductMsg.result().body())

            eventBus.request<String>("process.categories.delete", categoryId).onFailure {
              testContext.failNow(it)
            }.onComplete { deleteCategoryMsg ->
              assert(deleteCategoryMsg.succeeded())
              assertEquals("Category deleted successfully!", deleteCategoryMsg.result().body())

              eventBus.request<String>("process.textPages.delete", textPageId).onFailure {
                testContext.failNow(it)
              }.onComplete { deleteTextPageMsg ->
                assert(deleteTextPageMsg.succeeded())
                assertEquals("Text page deleted successfully", deleteTextPageMsg.result().body())

                eventBus.request<String>("process.url.deleteUrlKey", url, urlKeysDeliveryOptions).onFailure {
                  testContext.failNow(it)
                }.onComplete { deleteUrlKeyMsg ->
                  assert(deleteUrlKeyMsg.succeeded())
                  assertEquals("Url key deleted successfully", deleteUrlKeyMsg.result().body())

                  testContext.completeNow()
                }
              }
            }
          }
        }
      }
    }
  }

  private fun setAllJsons() {
    textPageUrl.textPagesId = textPageId
    categoryUrl.categoryId = categoryId
    productUrl.productId = productId
    fullUrl.textPage = textPage
    fullUrl.category = category
    fullUrl.product = product
  }

  private fun deployNeededVerticles(vertx: Vertx): MutableList<Future<Void>> {
    val verticleList: MutableList<Future<Void>> = emptyList<Future<Void>>().toMutableList()

    verticleList.add(deployWorkerVerticleHelper(
      vertx,
      UrlJdbcVerticle::class.qualifiedName.toString(), 5, 5
    ))
    verticleList.add(
      deployWorkerVerticleHelper(
        vertx,
        TextPagesJdbcVerticle::class.qualifiedName.toString(), 5, 5
      )
    )
    verticleList.add(deployWorkerVerticleHelper(
      vertx,
      CategoryJdbcVerticle::class.qualifiedName.toString(), 5, 5
    ))
    verticleList.add(deployWorkerVerticleHelper(
      vertx,
      ProductJdbcVerticle::class.qualifiedName.toString(), 5, 5
    ))

    return verticleList
  }

  private fun convertStringToPageType(pageType: String): PageType {
    return when (pageType) {
      "text_page" -> PageType.TEXT_PAGE
      "category" -> PageType.CATEGORY
      "product" -> PageType.PRODUCT
      else -> throw IllegalArgumentException("Invalid page type: $pageType")
    }
  }
}
