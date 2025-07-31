package com.ex_dock.ex_dock.database

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.backend.v1.router.docker.ServerHealth
import com.ex_dock.ex_dock.backend.v1.router.system.SystemVerticle
import com.ex_dock.ex_dock.database.account.*
import com.ex_dock.ex_dock.database.auth.AuthenticationVerticle
import com.ex_dock.ex_dock.database.backend_block.*
import com.ex_dock.ex_dock.database.category.*
import com.ex_dock.ex_dock.database.checkout.CheckoutJdbcVerticle
import com.ex_dock.ex_dock.database.home.HomeJdbcVerticle
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.image.ImageProduct
import com.ex_dock.ex_dock.database.product.*
import com.ex_dock.ex_dock.database.scope.ScopeJdbcVerticle
import com.ex_dock.ex_dock.database.server.ServerDataData
import com.ex_dock.ex_dock.database.server.ServerJDBCVerticle
import com.ex_dock.ex_dock.database.server.ServerVersionData
import com.ex_dock.ex_dock.database.service.PopulateException
import com.ex_dock.ex_dock.database.service.ServiceVerticle
//import com.ex_dock.ex_dock.database.service.ServiceVerticle
import com.ex_dock.ex_dock.database.template.Template
import com.ex_dock.ex_dock.database.template.TemplateJdbcVerticle
import com.ex_dock.ex_dock.database.text_pages.TextPages
import com.ex_dock.ex_dock.database.text_pages.TextPagesJdbcVerticle
import com.ex_dock.ex_dock.database.url.*
import com.ex_dock.ex_dock.frontend.cache.CacheVerticle
import com.ex_dock.ex_dock.helper.*
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials

class JDBCStarter : VerticleBase() {
  companion object {
    val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}
  }

  private var verticles: MutableList<Future<String>> = emptyList<Future<String>>().toMutableList()
  private val verticleIds = emptyList<String>().toMutableList()
  private lateinit var eventBus: EventBus

  override fun start(): Future<*> {
    addAllVerticles()

    return Future.all<String>(verticles)
      .onFailure { error ->
        logger.error { error.message }
      }
      .onSuccess { future ->
        logger.info { "All JDBC Verticles started successfully" }

        if (!MainVerticle.areCodecsRegistered) {
          MainVerticle.areCodecsRegistered = true
          getAllCodecClasses()
        }

        eventBus = vertx.eventBus()
        verticles.forEach { verticle ->
          verticleIds.add(verticle.result())
        }
        eventBus.registerVerticleIds(verticleIds)

        eventBus.request<String>("process.service.populateTemplates", "").onFailure {
          eventBus.sendError(
            PopulateException("Could not populate the database with standard data. Closing the server!"))
          throw PopulateException("Could not populate the database with standard data. Closing the server!")
        }.onSuccess {
          logger.info { "Database populated with standard data" }
          eventBus.request<String>("process.service.addAdminUser", "").onFailure {
            eventBus.sendError(
              PopulateException("Could not populate the database with standard data. Closing the server!"))
            throw PopulateException("Could not add admin user. Closing the server!")
          }.onSuccess {
            eventBus.request<String>("process.service.addTestProduct", "").onFailure {
              throw PopulateException("Could not add test product. Closing the server!")
            }.onSuccess {
              eventBus.request<String>("process.service.addProductInfoBackendBlock", "").onFailure {
                throw PopulateException("Could not add product info backend block. Closing the server!")
              }.onSuccess {
                eventBus.send("process.docker.serverHealth", ServerHealth.UP,
                  DeliveryOptions().setCodecName("ServerHealthCodec"))
              }
            }
          }
        }
      }
  }

  private fun addAllVerticles() {
    vertx.deployWorkerVerticleHelper(AuthenticationVerticle::class)

    verticles.add(vertx.deployWorkerVerticleHelper(AccountJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CategoryJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CheckoutJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(HomeJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ProductJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TextPagesJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ScopeJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ServerJDBCVerticle::class))
//    verticles.add(vertx.deployWorkerVerticleHelper(UrlJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ServiceVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CacheVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(BackendBlockJdbcVerticle::class))
//    verticles.add(vertx.deployWorkerVerticleHelper(ImageJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(SystemVerticle::class))
  }

  private fun getAllCodecClasses() {
    vertx.eventBus()
      .registerGenericCodec(ServerDataData::class)
      .registerGenericCodec(ServerVersionData::class)
      .registerGenericCodec(TextPages::class)
      .registerGenericCodec(PageIndex::class)
      .registerGenericCodec(FullUser::class)
      .registerGenericCodec(Template::class)
      .registerGenericCodec(Map::class)
      .registerGenericCodec(UsernamePasswordCredentials::class)
      .registerGenericCodec(BlockAttribute::class)
      .registerGenericCodec(Image::class)
      .registerGenericCodec(ImageProduct::class)
      .registerGenericCodec(ServerHealth::class)

      .registerGenericListCodec(FullUser::class)
  }

}
