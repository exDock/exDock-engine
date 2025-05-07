package com.ex_dock.ex_dock.database.category

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class CategoryJdbcVerticleTest {
  private lateinit var eventBus: EventBus

  private var categoryId: Int? = null

  private var category = Categories(
    categoryId = categoryId,
    upperCategory = null,
    name = "test name",
    shortDescription = "test description",
    description = "test description"
  )

  private lateinit var fullCategory: FullCategoryInfo
  private lateinit var categorySeo: CategoriesSeo
  private val categoryList: MutableList<Categories> = emptyList<Categories>().toMutableList()
  private val categorySeoList: MutableList<CategoriesSeo> = emptyList<CategoriesSeo>().toMutableList()
  private val fullCategoryList: MutableList<FullCategoryInfo> = emptyList<FullCategoryInfo>().toMutableList()
  private val categoriesDeliveryOptions = DeliveryOptions().setCodecName("CategoriesCodec")
  private val seoCategoriesDeliveryOptions = DeliveryOptions().setCodecName("CategoriesSeoCodec")

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(Categories::class))
      .registerCodec(GenericCodec(CategoriesSeo::class))
      .registerCodec(GenericCodec(CategoriesProducts::class))
      .registerCodec(GenericCodec(FullCategoryInfo::class))
    deployWorkerVerticleHelper(
      vertx,
      CategoryJdbcVerticle::class.qualifiedName.toString(), 5, 5
    ).onFailure {
      testContext.failNow(it)
    }.onComplete {
      eventBus.request<Categories>("process.categories.create", category, categoriesDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createCategoryMsg ->
        assert(createCategoryMsg.succeeded())
        categoryId = createCategoryMsg.result().body().categoryId
        category.categoryId = categoryId

        assertEquals(createCategoryMsg.result().body(), category)

        categorySeo = CategoriesSeo(
          categoryId = categoryId!!,
          metaTitle = "test meta_title",
          metaDescription = "test meta_description",
          metaKeywords = "test meta_keywords",
          pageIndex = getEnum("index, follow")
        )

        fullCategory = FullCategoryInfo(
          categories = category,
          categoriesSeo = categorySeo
        )

        categoryList.add(category)
        categorySeoList.add(categorySeo)
        fullCategoryList.add(fullCategory)

        eventBus.request<CategoriesSeo>("process.categories.createSeoCategory", categorySeo, seoCategoriesDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createSeoCategoryMsg ->
          assert(createSeoCategoryMsg.succeeded())
          assertEquals(categorySeo, createSeoCategoryMsg.result().body())

          testContext.completeNow()
        }
      }
    }
  }

  @Test
  fun testGetAllCategories(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<Categories>>("process.categories.getAll", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllCategoriesMsg ->
      assert(getAllCategoriesMsg.succeeded())
      assertEquals(categoryList, getAllCategoriesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetCategoryById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Categories>("process.categories.getById", categoryId).onFailure {
      testContext.failNow(it)
    }.onComplete { getCategoryByIdMsg ->
      assert(getCategoryByIdMsg.succeeded())
      assertEquals(category, getCategoryByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateCategory(vertx: Vertx, testContext: VertxTestContext) {
    val updatedCategory = Categories(
      categoryId = categoryId,
      upperCategory = null,
      name = "updated test name",
      shortDescription = "updated test description",
      description = "updated test description"
    )

    eventBus.request<Categories>("process.categories.edit", updatedCategory, categoriesDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateCategoryMsg ->
      assert(updateCategoryMsg.succeeded())
      assertEquals(updatedCategory, updateCategoryMsg.result().body())

      eventBus.request<Categories>("process.categories.getById", categoryId).onFailure {
        testContext.failNow(it)
      }.onComplete { getCategoryMsg ->
        assert(getCategoryMsg.succeeded())
        assertEquals(updatedCategory, getCategoryMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun getAllSeoCategories(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<CategoriesSeo>>("process.categories.getAllSeo", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllSeoCategoriesMsg ->
      assert(getAllSeoCategoriesMsg.succeeded())
      assertEquals(categorySeoList, getAllSeoCategoriesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetSeoCategoryById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<CategoriesSeo>("process.categories.getSeoById", categoryId).onFailure {
      testContext.failNow(it)
    }.onComplete { getSeoCategoryByIdMsg ->
      assert(getSeoCategoryByIdMsg.succeeded())
      assertEquals(categorySeo, getSeoCategoryByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateSeoCategory(vertx: Vertx, testContext: VertxTestContext) {
    val updatedCategorySeo = CategoriesSeo(
      categoryId = categoryId!!,
      metaTitle = "updated test meta_title",
      metaDescription = "updated test meta_description",
      metaKeywords = "updated test meta_keywords",
      pageIndex = getEnum("index, nofollow")
    )

    eventBus.request<CategoriesSeo>("process.categories.editSeoCategory", updatedCategorySeo, seoCategoriesDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateSeoCategoryMsg ->
      assert(updateSeoCategoryMsg.succeeded())
      assertEquals(updatedCategorySeo, updateSeoCategoryMsg.result().body())

      eventBus.request<JsonObject>("process.categories.getSeoById", categoryId).onFailure {
        testContext.failNow(it)
      }.onComplete { getSeoCategoryMsg ->
        assert(getSeoCategoryMsg.succeeded())
        assertEquals(updatedCategorySeo, getSeoCategoryMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetFullCategoryInfo(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<FullCategoryInfo>>("process.categories.getAllFullInfo", categoryId).onFailure {
      testContext.failNow(it)
    }.onComplete { getFullCategoryInfoMsg ->
      assert(getFullCategoryInfoMsg.succeeded())
      assertEquals(fullCategoryList, getFullCategoryInfoMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetFullCategoryInfoById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<FullCategoryInfo>("process.categories.getFullInfoById", categoryId).onFailure {
      testContext.failNow(it)
    }.onComplete { getFullCategoryByIdMsg ->
      assert(getFullCategoryByIdMsg.succeeded())
      assertEquals(fullCategory, getFullCategoryByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.categories.deleteSeoCategory", categoryId).onFailure {
      testContext.failNow(it)
    }.onComplete { deleteSeoCategoryMsg ->
      assert(deleteSeoCategoryMsg.succeeded())
      assertEquals("SEO category deleted successfully!", deleteSeoCategoryMsg.result().body())

      eventBus.request<String>("process.categories.delete", categoryId).onFailure {
        testContext.failNow(it)
      }.onComplete { deleteCategoryMsg ->
        assert(deleteCategoryMsg.succeeded())
        assertEquals("Category deleted successfully!", deleteCategoryMsg.result().body())
        testContext.completeNow()
      }
    }
  }

  private fun getEnum(name: String): PageIndex {
    when (name) {
      "index, follow" -> return PageIndex.IndexFollow
      "index, nofollow" -> return PageIndex.IndexNoFollow
      "noindex, follow" -> return PageIndex.NoIndexFollow
      "noindex, nofollow" -> return PageIndex.NoIndexNoFollow
    }

    return PageIndex.NoIndexNoFollow
  }
}
