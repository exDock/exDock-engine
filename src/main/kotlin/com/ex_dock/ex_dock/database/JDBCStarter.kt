package com.ex_dock.ex_dock.database

import com.ex_dock.ex_dock.database.account.*
import com.ex_dock.ex_dock.database.backend_block.*
import com.ex_dock.ex_dock.database.auth.AuthenticationVerticle
import com.ex_dock.ex_dock.database.category.*
import com.ex_dock.ex_dock.database.checkout.CheckoutJdbcVerticle
import com.ex_dock.ex_dock.database.home.HomeJdbcVerticle
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.image.ImageJdbcVerticle
import com.ex_dock.ex_dock.database.image.ImageProduct
import com.ex_dock.ex_dock.database.product.*
import com.ex_dock.ex_dock.database.scope.FullScope
import com.ex_dock.ex_dock.database.scope.ScopeJdbcVerticle
import com.ex_dock.ex_dock.database.scope.StoreView
import com.ex_dock.ex_dock.database.scope.Websites
import com.ex_dock.ex_dock.database.server.ServerDataData
import com.ex_dock.ex_dock.database.server.ServerJDBCVerticle
import com.ex_dock.ex_dock.database.server.ServerVersionData
import com.ex_dock.ex_dock.database.service.PopulateException
import com.ex_dock.ex_dock.database.service.ServiceVerticle
import com.ex_dock.ex_dock.database.template.Block
import com.ex_dock.ex_dock.database.template.Template
import com.ex_dock.ex_dock.database.template.TemplateJdbcVerticle
import com.ex_dock.ex_dock.database.text_pages.FullTextPages
import com.ex_dock.ex_dock.database.text_pages.TextPages
import com.ex_dock.ex_dock.database.text_pages.TextPagesJdbcVerticle
import com.ex_dock.ex_dock.database.text_pages.TextPagesSeo
import com.ex_dock.ex_dock.database.url.*
import com.ex_dock.ex_dock.frontend.cache.CacheVerticle
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import com.ex_dock.ex_dock.helper.registerGenericListCodec
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials

class JDBCStarter : AbstractVerticle() {

  private var verticles: MutableList<Future<Void>> = emptyList<Future<Void>>().toMutableList()
  private lateinit var eventBus: EventBus

  override fun start(starPromise: Promise<Void>) {
    addAllVerticles()

    Future.all(verticles)
      .onComplete {
        println("All JDBC verticles deployed")
        getAllCodecClasses()
        eventBus = vertx.eventBus()

        eventBus.request<String>("process.service.populateTemplates", "").onFailure {
          throw PopulateException("Could not populate the database with standard data. Closing the server!")
        }.onSuccess {
          println("Database populated with standard Data")
          eventBus.request<String>("process.service.addAdminUser", "").onFailure {
            throw PopulateException("Could not add admin user. Closing the server!")
          }.onSuccess {
            eventBus.request<String>("process.service.addTestProduct", "").onFailure {
              throw PopulateException("Could not add test product. Closing the server!")
            }.onSuccess {
              eventBus.request<String>("process.service.addProductInfoBackendBlock", "").onFailure {
                throw PopulateException("Could not add product info backend block. Closing the server!")
              }.onSuccess {
                starPromise.complete()
              }
            }
          }
        }
      }
      .onFailure { error ->
        println("Failed to deploy JDBC verticles: $error")
      }
  }

  private fun addAllVerticles() {
    verticles.add(vertx.deployWorkerVerticleHelper(AccountJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CategoryJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CheckoutJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(HomeJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TextPagesJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ScopeJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ServerJDBCVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(UrlJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductCompleteEavJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductGlobalEavJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductMultiSelectJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductStoreViewEavJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductWebsiteEavJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductCustomAttributesJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TemplateJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ServiceVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CacheVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(BackendBlockJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ImageJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(AuthenticationVerticle::class))
  }

  private fun getAllCodecClasses() {
    vertx.eventBus()
      .registerGenericCodec(MutableList::class)
      .registerGenericCodec(Categories::class)
      .registerGenericCodec(CategoriesSeo::class)
      .registerGenericCodec(CategoriesProducts::class)
      .registerGenericCodec(FullCategoryInfo::class)
      .registerGenericCodec(CustomProductAttributes::class)
      .registerGenericCodec(Products::class)
      .registerGenericCodec(EavGlobalBool::class)
      .registerGenericCodec(EavGlobalFloat::class)
      .registerGenericCodec(EavGlobalInt::class)
      .registerGenericCodec(EavGlobalMoney::class)
      .registerGenericCodec(EavGlobalMultiSelect::class)
      .registerGenericCodec(EavGlobalString::class)
      .registerGenericCodec(Eav::class)
      .registerGenericCodec(EavGlobalInfo::class)
      .registerGenericCodec(ProductsSeo::class)
      .registerGenericCodec(ProductsPricing::class)
      .registerGenericCodec(FullProduct::class)
      .registerGenericCodec(MultiSelectBool::class)
      .registerGenericCodec(MultiSelectFloat::class)
      .registerGenericCodec(MultiSelectString::class)
      .registerGenericCodec(MultiSelectInt::class)
      .registerGenericCodec(MultiSelectMoney::class)
      .registerGenericCodec(MultiSelectInfo::class)
      .registerGenericCodec(Websites::class)
      .registerGenericCodec(StoreView::class)
      .registerGenericCodec(EavStoreViewBool::class)
      .registerGenericCodec(EavStoreViewFloat::class)
      .registerGenericCodec(EavStoreViewString::class)
      .registerGenericCodec(EavStoreViewInt::class)
      .registerGenericCodec(EavStoreViewMoney::class)
      .registerGenericCodec(EavStoreViewInfo::class)
      .registerGenericCodec(EavStoreViewMultiSelect::class)
      .registerGenericCodec(EavWebsiteBool::class)
      .registerGenericCodec(EavWebsiteFloat::class)
      .registerGenericCodec(EavWebsiteString::class)
      .registerGenericCodec(EavWebsiteInt::class)
      .registerGenericCodec(EavWebsiteMoney::class)
      .registerGenericCodec(EavWebsiteInfo::class)
      .registerGenericCodec(EavWebsiteMultiSelect::class)
      .registerGenericCodec(FullScope::class)
      .registerGenericCodec(ServerDataData::class)
      .registerGenericCodec(ServerVersionData::class)
      .registerGenericCodec(TextPages::class)
      .registerGenericCodec(TextPagesSeo::class)
      .registerGenericCodec(PageIndex::class)
      .registerGenericCodec(FullTextPages::class)
      .registerGenericCodec(TextPageUrls::class)
      .registerGenericCodec(ProductUrls::class)
      .registerGenericCodec(CategoryUrls::class)
      .registerGenericCodec(FullUrlKeys::class)
      .registerGenericCodec(FullUrlRequestInfo::class)
      .registerGenericCodec(JoinList::class)
      .registerGenericCodec(UrlKeys::class)
      .registerGenericCodec(User::class)
      .registerGenericCodec(UserCreation::class)
      .registerGenericCodec(BackendPermissions::class)
      .registerGenericCodec(FullUser::class)
      .registerGenericCodec(Template::class)
      .registerGenericCodec(Block::class)
      .registerGenericCodec(Map::class)
      .registerGenericCodec(UsernamePasswordCredentials::class)
      .registerGenericCodec(Pair::class)
      .registerGenericCodec(BackendBlock::class)
      .registerGenericCodec(BlockAttribute::class)
      .registerGenericCodec(AttributeBlock::class)
      .registerGenericCodec(BlockId::class)
      .registerGenericCodec(EavAttributeBool::class)
      .registerGenericCodec(EavAttributeFloat::class)
      .registerGenericCodec(EavAttributeInt::class)
      .registerGenericCodec(EavAttributeMoney::class)
      .registerGenericCodec(EavAttributeString::class)
      .registerGenericCodec(EavAttributeMultiSelect::class)
      .registerGenericCodec(FullBlockInfo::class)
      .registerGenericCodec(FullEavAttribute::class)
      .registerGenericCodec(Image::class)
      .registerGenericCodec(ImageProduct::class)

      .registerGenericListCodec(FullUser::class)
  }

}
