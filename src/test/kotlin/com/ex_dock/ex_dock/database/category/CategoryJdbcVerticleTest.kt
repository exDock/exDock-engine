package com.ex_dock.ex_dock.database.category

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class CategoryJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val categoryDeliveryOptions = DeliveryOptions().setCodecName("CategoryInfoCodec")
  private val testCategory = CategoryInfo(
    categoryId = "123",
    upperCategory = "1",
    name = "testName",
    description = "testDescription",
    shortDescription = "testShortDescription",
    metaTitle = "testMetaTitle",
    metaDescription = "testMetaDescription",
    metaKeywords = "testMetaKeywords",
    pageIndex = PageIndex.IndexFollow,
    products = listOf("1", "2", "3")
  )

  @Test
  @DisplayName("Test the category classes functions")
  fun testCategoryClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testCategoryClassesFunctions")

    suite.test("testCategoryInfoToJson") { testContext ->
      val result = testCategory.toDocument()
      testContext.assertEquals(testCategory.categoryId, result.getString("_id"))
      testContext.assertEquals(testCategory.name, result.getString("name"))
    }.test("testCategoryInfoFromJson") { testContext ->
      val categoryJson = testCategory.toDocument()
      val category = CategoryInfo.fromJson(categoryJson)
      testContext.assertEquals(testCategory.categoryId, category.categoryId)
      testContext.assertEquals(testCategory.name, category.name)
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
  @DisplayName("Add the category to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(CategoryInfo::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      CategoryJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
            eventBus.request<CategoryInfo>("process.category.createCategory", testCategory, categoryDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.categoryId == testCategory.categoryId)
          assert(result.upperCategory == testCategory.upperCategory)
          assert(result.name == testCategory.name)
          assert(result.description == testCategory.description)
          assert(result.shortDescription == testCategory.shortDescription)
          assert(result.products.size == 3)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a category by id from the database")
  fun testGetCategoryById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.category.getCategoryById", testCategory.categoryId).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = CategoryInfo.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.categoryId == testCategory.categoryId)
          assert(result.upperCategory == testCategory.upperCategory)
          assert(result.name == testCategory.name)
          assert(result.description == testCategory.description)
          assert(result.shortDescription == testCategory.shortDescription)
          assert(result.products.size == 3)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating a category in the database")
  fun testUpdateCategory(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedCategory = testCategory.copy(name = "updatedName")
    eventBus.request<CategoryInfo>("process.category.editCategory", updatedCategory, categoryDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.name == updatedCategory.name)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the category from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.category.deleteCategory", testCategory.categoryId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Category deleted successfully")
        vertxTestContext.completeNow()
      }
    }
  }
}
