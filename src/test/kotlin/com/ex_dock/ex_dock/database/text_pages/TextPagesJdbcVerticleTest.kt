package com.ex_dock.ex_dock.database.text_pages

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.codec.GenericCodec
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
class TextPagesJdbcVerticleTest {
    private lateinit var eventBus: EventBus
    private var textPageId = -1
    private val textPagesDeliveryOptions = DeliveryOptions().setCodecName("TextPagesCodec")
    private val seoTextPagesDeliveryOptions = DeliveryOptions().setCodecName("TextPagesSeoCodec")

    private var textPage = TextPages(
      textPagesId = textPageId,
      name = "test name",
      shortText = "test short_text",
      text = "test text"
    )

  private var textPageSeo = TextPagesSeo(
    textPagesId = textPageId,
    metaTitle = "test meta_title",
    metaDescription = "test meta_description",
    metaKeywords = "test meta_keywords",
    pageIndex = PageIndex.NoIndexFollow
  )

  private lateinit var fullTextPage: FullTextPages

    @BeforeEach
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
      eventBus = vertx.eventBus()
        .registerCodec(GenericCodec(MutableList::class))
        .registerCodec(GenericCodec(TextPages::class))
        .registerCodec(GenericCodec(TextPagesSeo::class))
        .registerCodec(GenericCodec(PageIndex::class))
        .registerCodec(GenericCodec(FullTextPages::class))
      deployWorkerVerticleHelper(vertx,
        TextPagesJdbcVerticle::class.qualifiedName.toString(), 5, 5).onComplete {

      eventBus.request<TextPages>("process.textPages.create", textPage, textPagesDeliveryOptions).onFailure {
        testContext.failNow(it)
      }.onComplete { createPageMsg ->
        assert(createPageMsg.succeeded())
        textPage = createPageMsg.result().body()
        textPageId = textPage.textPagesId
        textPageSeo.textPagesId = textPageId

        fullTextPage = FullTextPages(
          textPage,
          textPageSeo
        )

        eventBus.request<TextPagesSeo>("process.textPages.createSeoTextPage", textPageSeo, seoTextPagesDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createSeoMsg ->
          assert(createSeoMsg.succeeded())

          testContext.completeNow()
        }
      }
      }
    }

    @Test
    fun testGetAllPages(vertx: Vertx, testContext: VertxTestContext) {
      eventBus.request<MutableList<TextPages>>("process.textPages.getAll", "").onFailure {
        testContext.failNow(it)
      }.onComplete {getAllPagesMsg ->
        assert(getAllPagesMsg.succeeded())
        assertEquals(mutableListOf(textPage), getAllPagesMsg.result().body())

        testContext.completeNow()
      }
    }

  @Test
  fun testGetPageById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<TextPages>("process.textPages.getById", textPageId).onFailure {
        testContext.failNow(it)
      }.onComplete {getPageByIdMsg ->
        assert(getPageByIdMsg.succeeded())
        assertEquals(textPage, getPageByIdMsg.result().body())

        testContext.completeNow()
      }
    }

  @Test
  fun testUpdatePage(vertx: Vertx, testContext: VertxTestContext) {
    val updatedTextPage = TextPages(
      textPagesId = textPageId,
      name = "updated name",
      shortText = "updated short_text",
      text = "updated text"
    )

    eventBus.request<TextPages>("process.textPages.update", updatedTextPage, textPagesDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updatePageMsg ->
      assert(updatePageMsg.succeeded())

      eventBus.request<TextPages>("process.textPages.getById", textPageId).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedPageMsg ->
        assert(getUpdatedPageMsg.succeeded())
        assertEquals(updatedTextPage, getUpdatedPageMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllSeoTextPages(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<TextPagesSeo>>("process.textPages.getAllSeoTextPages", "").onFailure {
        testContext.failNow(it)
      }.onComplete { getAllSeoPagesMsg ->
        assert(getAllSeoPagesMsg.succeeded())
        assertEquals(mutableListOf(textPageSeo), getAllSeoPagesMsg.result().body())

        testContext.completeNow()
      }

  }

  @Test
  fun testGetSeoPageById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<TextPagesSeo>("process.textPages.getSeoTextPageBySeoTextPageId", textPageId).onFailure {
        testContext.failNow(it)
      }.onComplete { getSeoPageByIdMsg ->
        assert(getSeoPageByIdMsg.succeeded())
        assertEquals(textPageSeo, getSeoPageByIdMsg.result().body())

        testContext.completeNow()
      }

  }

  @Test
  fun testUpdateSeoPage(vertx: Vertx, testContext: VertxTestContext) {
    val updatedSeoTextPage = TextPagesSeo(
      textPagesId = textPageId,
      metaTitle = "updated meta_title",
      metaDescription = "updated meta_description",
      metaKeywords = "updated meta_keywords",
      pageIndex = PageIndex.NoIndexFollow
    )

    eventBus.request<TextPagesSeo>("process.textPages.updateSeoTextPage", updatedSeoTextPage, seoTextPagesDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateSeoPageMsg ->
      assert(updateSeoPageMsg.succeeded())

      eventBus.request<TextPagesSeo>("process.textPages.getSeoTextPageBySeoTextPageId", textPageId).onFailure {
        testContext.failNow(it)
      }.onComplete { getUpdatedSeoPageMsg ->
        assert(getUpdatedSeoPageMsg.succeeded())
        assertEquals(updatedSeoTextPage, getUpdatedSeoPageMsg.result().body())
        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetFullTextPages(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<FullTextPages>>("process.textPages.getAllFullTextPages", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getFullTextPagesMsg ->
      assert(getFullTextPagesMsg.succeeded())
      assertEquals(mutableListOf(fullTextPage), getFullTextPagesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetFullTextPageById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<FullTextPages>("process.textPages.getFullTextPageByFullTextPageId", textPageId).onFailure {
      testContext.failNow(it)
    }.onComplete { getFullTextPageByIdMsg ->
      assert(getFullTextPageByIdMsg.succeeded())
      assertEquals(fullTextPage, getFullTextPageByIdMsg.result().body())

      testContext.completeNow()
    }
  }

    @AfterEach
    fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
      eventBus.request<String>("process.textPages.deleteSeoTextPage", textPageId).onFailure {
        testContext.failNow(it)
      }.onComplete { deletePageMsg ->
        assert(deletePageMsg.succeeded())

        eventBus.request<String>("process.textPages.delete", textPageId).onFailure {
          testContext.failNow(it)
        }.onComplete { deleteSeoMsg ->
          assert(deleteSeoMsg.succeeded())

          testContext.completeNow()
        }
      }
    }
}
