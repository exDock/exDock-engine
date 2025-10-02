package com.ex_dock.ex_dock.database

//import com.ex_dock.ex_dock.database.service.ServiceVerticle
import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.backend.v1.router.docker.ServerHealth
import com.ex_dock.ex_dock.backend.v1.router.system.SystemVerticle
import com.ex_dock.ex_dock.database.account.AccountJdbcVerticle
import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.auth.AuthenticationVerticle
import com.ex_dock.ex_dock.database.backend_block.BackendBlockJdbcVerticle
import com.ex_dock.ex_dock.database.backend_block.BlockAttribute
import com.ex_dock.ex_dock.database.category.CategoryJdbcVerticle
import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.checkout.CheckoutJdbcVerticle
import com.ex_dock.ex_dock.database.home.HomeJdbcVerticle
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.database.image.ImageProduct
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.product.ProductJdbcVerticle
import com.ex_dock.ex_dock.database.sales.*
import com.ex_dock.ex_dock.database.scope.ScopeJdbcVerticle
import com.ex_dock.ex_dock.database.server.ServerDataData
import com.ex_dock.ex_dock.database.server.ServerJDBCVerticle
import com.ex_dock.ex_dock.database.server.ServerVersionData
import com.ex_dock.ex_dock.database.service.ServiceVerticle
import com.ex_dock.ex_dock.database.template.Template
import com.ex_dock.ex_dock.database.template.TemplateJdbcVerticle
import com.ex_dock.ex_dock.database.text_pages.TextPages
import com.ex_dock.ex_dock.database.text_pages.TextPagesJdbcVerticle
import com.ex_dock.ex_dock.database.url.UrlJdbcVerticle
import com.ex_dock.ex_dock.frontend.cache.CacheVerticle
import com.ex_dock.ex_dock.frontend.template_engine.TemplateEngineVerticle
import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateData
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import com.ex_dock.ex_dock.helper.registerGenericListCodec
import com.ex_dock.ex_dock.helper.registerVerticleIds
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus

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

        eventBus.send(
          "process.docker.serverHealth", ServerHealth.UP,
          DeliveryOptions().setCodecName("ServerHealthCodec")
        )
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
    verticles.add(vertx.deployWorkerVerticleHelper(UrlJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(ServiceVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(CacheVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(BackendBlockJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(SystemVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TemplateJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TemplateEngineVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(SalesJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TemplateJdbcVerticle::class))
    verticles.add(vertx.deployWorkerVerticleHelper(TemplateEngineVerticle::class, workerPoolSize = 5, poolName = "template-cache-isolation-pool"))
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
      .registerGenericCodec(BlockAttribute::class)
      .registerGenericCodec(Image::class)
      .registerGenericCodec(ImageProduct::class)
      .registerGenericCodec(ServerHealth::class)
      .registerGenericCodec(ProductInfo::class)
      .registerGenericCodec(Order::class)
      .registerGenericCodec(Invoice::class)
      .registerGenericCodec(CreditMemo::class)
      .registerGenericCodec(Transaction::class)
      .registerGenericCodec(Shipment::class)
      .registerGenericCodec(List::class)
      .registerGenericCodec(SingleUseTemplateData::class)

      .registerGenericListCodec(FullUser::class)
  }

}
