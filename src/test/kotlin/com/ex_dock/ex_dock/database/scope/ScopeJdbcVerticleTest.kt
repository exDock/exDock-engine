package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ScopeJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private var storeViewId = -1
  private var websiteId: Int? = -1
  private val websiteDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("WebsitesCodec")
  private val storeViewDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("StoreViewCodec")
  private val websiteList: MutableList<Websites> = emptyList<Websites>().toMutableList()
  private var storeViewList: MutableList<StoreView> = emptyList<StoreView>().toMutableList()
  private val fullScopeList: MutableList<FullScope> = emptyList<FullScope>().toMutableList()

  private var website = Websites(
    websiteId = websiteId,
    websiteName = "Test Name"
  )

  private var storeView = StoreView(
    storeViewId = storeViewId,
    storeViewName = "Test Store View Name",
    websiteId = websiteId!!
  )

  private var fullScope = FullScope(
    website = website,
    storeView = storeView
  )

  @BeforeEach
  fun setUp(vertx: Vertx, testContext: VertxTestContext) {
    eventBus = vertx.eventBus()
      .registerCodec(GenericCodec(MutableList::class))
      .registerCodec(GenericCodec(Websites::class))
      .registerCodec(GenericCodec(StoreView::class))
      .registerCodec(GenericCodec(FullScope::class))
      deployWorkerVerticleHelper(
        vertx,
        ScopeJdbcVerticle::class.qualifiedName.toString(), 5, 5
      ).onComplete {
        eventBus.request<Websites>("process.scope.createWebsite", website, websiteDeliveryOptions).onFailure {
          testContext.failNow(it)
        }.onComplete { createWebsiteMsg ->
          assert(createWebsiteMsg.succeeded())
          website = createWebsiteMsg.result().body()
          websiteId = website.websiteId
          storeView.websiteId = websiteId!!

          eventBus.request<StoreView>("process.scope.createStoreView", storeView, storeViewDeliveryOptions).onFailure {
            testContext.failNow(it)
          }.onComplete { createStoreViewMsg ->
            assert(createStoreViewMsg.succeeded())
            storeView = createStoreViewMsg.result().body()
            storeViewId = storeView.storeViewId
            fullScope.website = website
            fullScope.storeView = storeView

            websiteList.add(website)
            storeViewList.add(storeView)
            fullScopeList.add(fullScope)

            testContext.completeNow()
          }
        }
      }
    }
  @Test
  fun testGetAllWebsites(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<Websites>>("process.scope.getAllWebsites", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllWebsitesMsg ->
      assert(getAllWebsitesMsg.succeeded())
      assertEquals(websiteList, getAllWebsitesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetWebsiteById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<Websites>("process.scope.getWebsiteById", websiteId).onFailure {
      testContext.failNow(it)
    }.onComplete { getWebsiteByIdMsg ->
      assert(getWebsiteByIdMsg.succeeded())
      assertEquals(website, getWebsiteByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateWebsite(vertx: Vertx, testContext: VertxTestContext) {
    val updatedWebsite = Websites(
      websiteId = websiteId,
      websiteName = "Updated Test Name"
    )

    eventBus.request<Websites>("process.scope.editWebsite", updatedWebsite, websiteDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateWebsiteMsg ->
      assert(updateWebsiteMsg.succeeded())
      assertEquals(updatedWebsite, updateWebsiteMsg.result().body())

      eventBus.request<Websites>("process.scope.getWebsiteById", websiteId).onFailure {
        testContext.failNow(it)
      }.onComplete { getWebsiteByIdMsg ->
        assert(getWebsiteByIdMsg.succeeded())
        assertEquals(updatedWebsite, getWebsiteByIdMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllStoreViews(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<StoreView>>("process.scope.getAllStoreViews", websiteId).onFailure {
      testContext.failNow(it)
    }.onComplete { getAllStoreViewsMsg ->
      assert(getAllStoreViewsMsg.succeeded())
      assertEquals(storeViewList, getAllStoreViewsMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetStoreViewById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<StoreView>("process.scope.getStoreViewById", storeViewId).onFailure {
      testContext.failNow(it)
    }.onComplete { getStoreViewByIdMsg ->
      assert(getStoreViewByIdMsg.succeeded())
      assertEquals(storeView, getStoreViewByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testUpdateStoreView(vertx: Vertx, testContext: VertxTestContext) {
    val updatedStoreView = StoreView(
      storeViewId = storeViewId,
      storeViewName = "Updated Test Store View Name",
      websiteId = websiteId!!
    )

    eventBus.request<StoreView>("process.scope.editStoreView", updatedStoreView, storeViewDeliveryOptions).onFailure {
      testContext.failNow(it)
    }.onComplete { updateStoreViewMsg ->
      assert(updateStoreViewMsg.succeeded())
      assertEquals(updatedStoreView, updateStoreViewMsg.result().body())

      eventBus.request<StoreView>("process.scope.getStoreViewById", storeViewId).onFailure {
        testContext.failNow(it)
      }.onComplete { getStoreViewByIdMsg ->
        assert(getStoreViewByIdMsg.succeeded())
        assertEquals(updatedStoreView, getStoreViewByIdMsg.result().body())

        testContext.completeNow()
      }
    }
  }

  @Test
  fun testGetAllFullScopes(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<MutableList<FullScope>>("process.scope.getAllScopes", "").onFailure {
      testContext.failNow(it)
    }.onComplete { getAllFullScopesMsg ->
      assert(getAllFullScopesMsg.succeeded())
      assertEquals(fullScopeList, getAllFullScopesMsg.result().body())

      testContext.completeNow()
    }
  }

  @Test
  fun testGetFullScopeById(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<FullScope>("process.scope.getScopeById", storeViewId).onFailure {
      testContext.failNow(it)
    }.onComplete { getFullScopeByIdMsg ->
      assert(getFullScopeByIdMsg.succeeded())
      assertEquals(fullScope, getFullScopeByIdMsg.result().body())

      testContext.completeNow()
    }
  }

  @AfterEach
  fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
    eventBus.request<String>("process.scope.deleteStoreView", storeViewId).onFailure {
      testContext.failNow(it)
    }.onComplete { storeViewMsg ->
      assert(storeViewMsg.succeeded())
      assertEquals("Store view deleted successfully", storeViewMsg.result().body())

      eventBus.request<String>("process.scope.deleteWebsite", websiteId).onFailure {
        testContext.failNow(it)
      }.onComplete { websiteMsg ->
        assert(websiteMsg.succeeded())
        assertEquals("Website deleted successfully", websiteMsg.result().body())

        testContext.completeNow()
      }
    }
  }
}
