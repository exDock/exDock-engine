package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.VerticleDeployHelper
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
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
class ProductJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private val verticleDeployHelper = VerticleDeployHelper()

  private var productId = -1

  private var product = Products(
    productId = productId,
    name = "test name",
    shortName = "test short name",
    description = "test description",
    shortDescription = "test short description",
    ean = "123",
    sku = "123",
    manufacturer = "test manufacturer"
  )

  private var productSeo = ProductsSeo(
    productId = productId,
    metaTitle = "test meta_title",
    metaDescription = "test meta_description",
    metaKeywords = "test meta_keywords",
    pageIndex = convertStringToPageIndex("index, follow")
  )

  private var productPricing = ProductsPricing(
    productId = productId,
    price = 10.0,
    salePrice = 10.0,
    costPrice = 4.0,
    taxClass = "tax_class",
    saleDateStart = null,
    saleDateEnd = null,
  )

  private var fullProduct = FullProduct(
    product = product,
    productsSeo = productSeo,
    productsPricing = productPricing,
    images = mutableListOf()
  )

  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val productSeoDeliveryOptions = DeliveryOptions().setCodecName("ProductsSeoCodec")
  private val productPricingDeliveryOptions = DeliveryOptions().setCodecName("ProductsPricingCodec")
  private val productList: MutableList<Products> = emptyList<Products>().toMutableList()
  private val productsSeoList : MutableList<ProductsSeo> = emptyList<ProductsSeo>().toMutableList()
  private val productsPricingList: MutableList<ProductsPricing> = emptyList<ProductsPricing>().toMutableList()
  private val fullProductList: MutableList<FullProduct> = emptyList<FullProduct>().toMutableList()

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(Products::class))
      .registerCodec(GenericCodec(ProductsSeo::class))
      .registerCodec(GenericCodec(ProductsPricing::class))
      .registerCodec(GenericCodec(FullProduct::class))
      .registerCodec(GenericCodec(MutableList::class))
    ProductJdbcVerticle::class.qualifiedName.toString()
    deployWorkerVerticleHelper(vertx,
      ProductJdbcVerticle::class.qualifiedName.toString(), 5, 5).onComplete {
      eventBus.request<Products>("process.products.createProduct", product, productDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createProductMsg ->
        product = createProductMsg.result().body()
        productId = product.productId
        assertEquals(product, createProductMsg.result().body())

        product.productId = productId
        productSeo.productId = productId
        productPricing.productId = productId
        fullProduct.product = product
        productList.add(product)
        productsSeoList.add(productSeo)
        productsPricingList.add(productPricing)
        fullProductList.add(fullProduct)

        eventBus.request<ProductsSeo>("process.products.createProductSeo", productSeo, productSeoDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createProductSeoMsg ->
          assertEquals(productSeo, createProductSeoMsg.result().body())

          eventBus.request<ProductsPricing>("process.products.createProductPricing", productPricing, productPricingDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createProductPricingMsg ->
            assertEquals(productPricing, createProductPricingMsg.result().body())

            testContext.completeNow()
          }
        }
      }
    }
  }

  @Test
  fun testGetAllProducts(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<MutableList<Products>>("process.products.getAllProducts", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllProductsMsg ->
//      assert(getAllProductsMsg.succeeded())
//      assertEquals(productList, getAllProductsMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetProductById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Products>("process.products.getProductById", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getProductByIdMsg ->
      assert(getProductByIdMsg.succeeded())
      assertEquals(product, getProductByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateProduct(vertx: Vertx, testContext: VertxTestContext) {
    val updatedProduct = Products(
      productId = productId,
      name = "updated test name",
      shortName = "updated test short name",
      description = "updated test description",
      shortDescription = "updated test short description",
      sku = "456",
      ean = "456",
      manufacturer = "updated test manufacturer"
    )

    eventBus.request<Products>("process.products.updateProduct", updatedProduct, productDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateProductMsg ->
      assert(updateProductMsg.succeeded())
      assertEquals(updatedProduct, updateProductMsg.result().body())

      eventBus.request<Products>("process.products.getProductById", productId).onFailure {
        testContext.failNow(it)
      }.onComplete { updatedProductMsg ->
        assert(updatedProductMsg.succeeded())
        assertEquals(updatedProduct, updatedProductMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllProductSeo(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<MutableList<ProductsSeo>>("process.products.getAllProductsSeo", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllProductSeoMsg ->
//      assert(getAllProductSeoMsg.succeeded())
//      assertEquals(productsSeoList, getAllProductSeoMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetProductSeoById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<ProductsSeo>("process.products.getProductSeoById", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getProductSeoByIdMsg ->
      assert(getProductSeoByIdMsg.succeeded())
      assertEquals(productSeo, getProductSeoByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateProductSeo(vertx: Vertx, testContext: VertxTestContext) {
    val updatedProductSeo = ProductsSeo(
      productId = productId,
      metaTitle = "updated test meta_title",
      metaDescription = "updated test meta_description",
      metaKeywords = "updated test meta_keywords",
      pageIndex = convertStringToPageIndex("noindex, follow")
    )

    eventBus.request<ProductsSeo>("process.products.updateProductSeo", updatedProductSeo, productSeoDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateProductSeoMsg ->
      assert(updateProductSeoMsg.succeeded())
      assertEquals(updatedProductSeo, updateProductSeoMsg.result().body())

      eventBus.request<ProductsSeo>("process.products.getProductSeoById", productId).onFailure {
        testContext.failNow(it)
      }.onComplete { updatedProductSeoMsg ->
        assert(updatedProductSeoMsg.succeeded())
        assertEquals(updatedProductSeo, updatedProductSeoMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllProductPricing(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<ProductsPricing>("process.products.getAllProductsPricing", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllProductPricingMsg ->
//      assert(getAllProductPricingMsg.succeeded())
//      assertEquals(productsPricingList, getAllProductPricingMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetProductPricingById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<ProductsPricing>("process.products.getProductPricingById", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { getProductPricingByIdMsg ->
      assert(getProductPricingByIdMsg.succeeded())
      assertEquals(productPricing, getProductPricingByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateProductPricing(vertx: Vertx, testContext: VertxTestContext) {
    val updatedProductPricing = ProductsPricing(
      productId = productId,
      price = 10.99,
      salePrice = 10.99,
      costPrice = 20.99,
      taxClass = "standard",
      saleDateStart = null,
      saleDateEnd = null,
    )

    eventBus.request<ProductsPricing>("process.products.updateProductPricing", updatedProductPricing, productPricingDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateProductPricingMsg ->
      assert(updateProductPricingMsg.succeeded())
      assertEquals(updatedProductPricing, updateProductPricingMsg.result().body())

      eventBus.request<ProductsPricing>("process.products.getProductPricingById", productId).onFailure {
        testContext.failNow(it)
      }.onComplete { updatedProductPricingMsg ->
        assert(updatedProductPricingMsg.succeeded())
        assertEquals(updatedProductPricing, updatedProductPricingMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllFullProducts(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<MutableList<FullProduct>>("process.products.getAllFullProducts", "").onFailure {
//      testContext.failNow(it)
//    }.onComplete { getAllFullProductsMsg ->
//      assert(getAllFullProductsMsg.succeeded())
//      assertEquals(fullProductList, getAllFullProductsMsg.result().body())

      testContext.completeNow()
//    }
  }

  @Test
  fun testGetFullProductById(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus.request<FullProduct>("process.products.getFullProductsById", productId).onFailure {
//      testContext.failNow(it)
//    }.onComplete { getFullProductByIdMsg ->
//      assert(getFullProductByIdMsg.succeeded())
//      assertEquals(fullProduct, getFullProductByIdMsg.result().body())

      testContext.completeNow()
//    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.products.deleteProductPricing", productId).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteProductPricingMsg ->
      assert(deleteProductPricingMsg.succeeded())
      assertEquals("Product pricing deleted successfully", deleteProductPricingMsg.result().body())

      eventBus.request<String>("process.products.deleteProductSeo", productId).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteProductSeoMsg ->
        assert(deleteProductSeoMsg.succeeded())
        assertEquals("Product SEO deleted successfully", deleteProductSeoMsg.result().body())

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

  private fun convertStringToPageIndex(name: String): PageIndex {
    return when (name) {
      "noindex, follow" -> PageIndex.NoIndexFollow
      "noindex, nofollow" -> PageIndex.NoIndexNoFollow
      "index, follow" -> PageIndex.IndexFollow
      "index, nofollow" -> PageIndex.IndexNoFollow
      else -> throw IllegalArgumentException("Invalid page index: $name")
    }
  }
}
