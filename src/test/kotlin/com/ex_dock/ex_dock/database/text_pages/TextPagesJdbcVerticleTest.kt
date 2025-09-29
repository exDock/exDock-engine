package com.ex_dock.ex_dock.database.text_pages

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
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
class TextPagesJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val textPagesDeliveryOptions = DeliveryOptions().setCodecName("TextPagesCodec")
  private val testTextPage = TextPages(
    textPagesId = "123",
    name = "testName",
    shortText = "testShortText",
    text = "testText",
    metaTitle = "testMetaTitle",
    metaDescription = "testMetaDescription",
    metaKeywords = "testMetaKeywords",
    pageIndex = PageIndex.IndexNoFollow
  )

  @Test
  @DisplayName("Test the text pages classes functions")
  fun testTextPagesClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testTextPagesClassesFunctions")

    suite.test("testTextPagesToJson") { testContext ->
      val result = testTextPage.toDocument()
      testContext.assertEquals(testTextPage.textPagesId, result.getString("_id"))
      testContext.assertEquals(testTextPage.name, result.getString("name"))
    }.test("testTextPagesFromJson") { testContext ->
      val textPageJson = testTextPage.toDocument()
      val textPage = TextPages.fromJson(textPageJson)
      testContext.assertEquals(testTextPage.textPagesId, textPage.textPagesId)
      testContext.assertEquals(testTextPage.name, textPage.name)
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
  @DisplayName("Add the text page to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(TextPages::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      TextPagesJdbcVerticle::class.qualifiedName.toString(),
      TextPagesJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<TextPages>("process.text_pages.createTextPage", testTextPage, textPagesDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.textPagesId == testTextPage.textPagesId)
          assert(result.name == testTextPage.name)
          assert(result.shortText == testTextPage.shortText)
          assert(result.text == testTextPage.text)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a text page by id from the database")
  fun testGetTextPageById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.text_pages.getTextPageById", testTextPage.textPagesId).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = TextPages.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.textPagesId == testTextPage.textPagesId)
          assert(result.name == testTextPage.name)
          assert(result.shortText == testTextPage.shortText)
          assert(result.text == testTextPage.text)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating a text page in the database")
  fun testUpdateTextPage(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedTextPage = testTextPage.copy(name = "updatedName")
    eventBus.request<TextPages>("process.text_pages.updateTextPage", updatedTextPage, textPagesDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.name == updatedTextPage.name)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the text page from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.text_pages.deleteTextPage", testTextPage.textPagesId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Successfully deleted text page")
        vertxTestContext.completeNow()
      }
    }
  }
}
