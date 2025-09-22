package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ProductJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductInfoCodec")
  private val testProduct = ProductInfo(
    productId = "123",
    name = "testName",
    shortName = "testShortName",
    description = "testDescription",
    shortDescription = "testShortDescription",
    sku = "testSku",
    ean = "testEan",
    location = "testLocation",
    manufacturer = "testManufacturer",
    metaTitle = "testMetaTitle",
    metaDescription = "testMetaDescription",
    metaKeywords = "testMetaKeywords",
    pageIndex = PageIndex.IndexFollow,
    price = 1.0,
    salePrice = 2.0,
    costPrice = 3.0,
    taxClass = "testTaxClass",
    saleDates = listOf("2025-01-01", "2025-12-31"),
    categories = listOf("1", "2", "3"),
    attributes = listOf(Pair("testAttribute", "testValue")),
    images = listOf(
      Image(
        imageUrl = "testUrl",
        imageName = "testImageName",
        imageExtensions = "testExtension"
      )
    )
  )

  @Test
  @DisplayName("Test the product classes functions")
  fun testProductClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testProductClassesFunctions")

    suite.test("testProductInfoToJson") { testContext ->
      val result = testProduct.toDocument()
      testContext.assertEquals(testProduct.productId, result.getString("_id"))
      testContext.assertEquals(testProduct.name, result.getString("name"))
    }.test("testProductInfoFromJson") { testContext ->
      val productJson = testProduct.toDocument()
      val product = ProductInfo.fromJson(productJson)
      testContext.assertEquals(testProduct.productId, product.productId)
      testContext.assertEquals(testProduct.name, product.name)
    }

    suite.run(vertx).handler { res ->
      if (res.succeeded()) {
        context.completeNow()
      } else {
        context.failNow(res.cause())
      }
    }
  }

  @BeforeEach
  @DisplayName("Add the product to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(ProductInfo::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      ProductJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<ProductInfo>("process.product.createProduct", testProduct, productDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.productId == testProduct.productId)
          assert(result.name == testProduct.name)
          assert(result.shortName == testProduct.shortName)
          assert(result.description == testProduct.description)
          assert(result.shortDescription == testProduct.shortDescription)
          assert(result.sku == testProduct.sku)
          assert(result.ean == testProduct.ean)
          assert(result.location == testProduct.location)
          assert(result.manufacturer == testProduct.manufacturer)
          assert(result.metaTitle == testProduct.metaTitle)
          assert(result.metaDescription == testProduct.metaDescription)
          assert(result.metaKeywords == testProduct.metaKeywords)
          assert(result.pageIndex == testProduct.pageIndex)
          assert(result.price == testProduct.price)
          assert(result.salePrice == testProduct.salePrice)
          assert(result.costPrice == testProduct.costPrice)
          assert(result.taxClass == testProduct.taxClass)
          assert(result.saleDates.size == 2)
          assert(result.categories.size == 3)
          assert(result.attributes.size == 1)
          assert(result.images.size == 1)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a product by id from the database")
  fun testGetProductById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<ProductInfo>("process.product.createProduct", testProduct, productDeliveryOptions).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess { message ->
      val result = message.body() as ProductInfo
      vertxTestContext.verify { ->
        assert(result.productId == testProduct.productId)
        assert(result.name == testProduct.name)
        assert(result.shortName == testProduct.shortName)
        assert(result.description == testProduct.description)
        assert(result.shortDescription == testProduct.shortDescription)
        assert(result.sku == testProduct.sku)
        assert(result.ean == testProduct.ean)
        assert(result.location == testProduct.location)
        assert(result.manufacturer == testProduct.manufacturer)
        assert(result.metaTitle == testProduct.metaTitle)
        assert(result.metaDescription == testProduct.metaDescription)
        assert(result.metaKeywords == testProduct.metaKeywords)
        assert(result.pageIndex == testProduct.pageIndex)
        assert(result.price == testProduct.price)
        assert(result.salePrice == testProduct.salePrice)
        assert(result.costPrice == testProduct.costPrice)
        assert(result.taxClass == testProduct.taxClass)
        assert(result.saleDates.size == 2)
        assert(result.categories.size == 3)
        assert(result.attributes.size == 1)
        assert(result.images.size == 1)
        vertxTestContext.completeNow()
      }
    }
  }

  @Test
  @DisplayName("Test updating a product in the database")
  fun testUpdateProduct(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedProduct = testProduct.copy(name = "updatedName")
    eventBus.request<ProductInfo>("process.product.updateProduct", updatedProduct, productDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.name == updatedProduct.name)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the product from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.product.deleteProduct", testProduct.productId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Product deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
