package com.ex_dock.ex_dock

import com.ex_dock.ex_dock.backend.enableBackendRouter
import com.ex_dock.ex_dock.frontend.account.router.initAccount
import com.ex_dock.ex_dock.frontend.category.router.initCategory
import com.ex_dock.ex_dock.frontend.checkout.router.initCheckout
import com.ex_dock.ex_dock.frontend.home.router.initHome
import com.ex_dock.ex_dock.frontend.product.router.initProduct
import com.ex_dock.ex_dock.frontend.router.enableFrontendRouter
import com.ex_dock.ex_dock.frontend.text_pages.router.initTextPages
import com.ex_dock.ex_dock.helper.load
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import com.ex_dock.ex_dock.helper.sendError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.http.CookieSameSite
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.SessionStore
import java.util.*

class MainVerticle : VerticleBase() {
  companion object {
    val logger = KotlinLogging.logger {}
    var areCodecsRegistered = false
  }
  private val deployedVerticleIds = emptyList<String>().toMutableList()

  private lateinit var props : Properties

  /**
  * This function is the entry point for the Vert.x application. It starts an HTTP server and listens on port 8888.
  *
  * @return Nothing is returned from this function.
  */
  override fun start(): Future<*> {
    props = Properties().load()

    vertx.deployVerticle(ExtensionsLauncher())
      .onSuccess{ verticleId ->
        logger.info { "MainVerticle started successfully" }
        deployedVerticleIds.add(verticleId)
      }
      .onFailure { err ->
        logger.error { "Failed to start MainVerticle: $err" }
      }

    val eventBus = vertx.eventBus()
    val mainRouter : Router = Router.router(vertx)
    val frontendRouter : Router = Router.router(vertx)
    val store = SessionStore.create(vertx)
    val sessionHandler = SessionHandler.create(store)

    eventBus.registerGenericCodec(List::class)
    eventBus.consumer<List<String>>("process.main.registerVerticleId").handler { message ->
      val verticleIds: List<String> = message.body()
      verticleIds.forEach { value ->
        deployedVerticleIds.add(value)
      }
      message.reply("")
    }

    eventBus.consumer<Unit>("process.main.redeployVerticles").handler { message ->
      deployedVerticleIds.forEach { verticleId ->
        vertx.undeploy(verticleId)
        logger.info { "Undeployed verticle with ID: $verticleId" }
      }
      deployedVerticleIds.clear()

      vertx.deployVerticle(ExtensionsLauncher()).onFailure { error ->
        logger.error { "Failed to deploy ExtensionsLauncher verticle: $error" }
        message.fail(500, "Failed to deploy ExtensionsLauncher verticle")
      }.onSuccess { verticleId ->
        deployedVerticleIds.add(verticleId)
        message.reply("")
      }
    }

    sessionHandler.setCookieSameSite(CookieSameSite.STRICT)

    mainRouter.route().handler(sessionHandler)
    mainRouter.route().handler(BodyHandler.create())

    mainRouter.enableBackendRouter(vertx, logger)

    mainRouter.initHome(eventBus)
    mainRouter.initProduct(vertx)
    mainRouter.initCategory(vertx)
    mainRouter.initTextPages(vertx)
    mainRouter.initCheckout(vertx)
    mainRouter.initAccount(vertx)

    frontendRouter.enableFrontendRouter(vertx, logger)

    vertx.createHttpServer()
      .requestHandler(frontendRouter)
      .listen(props.getProperty("FRONTEND_PORT").toInt()).onFailure {
        logger.error { "Failed to start HTTP server: $it" }
        vertx.eventBus().sendError(Exception("Failed to start the HTTP server"))
        }.onSuccess { http ->
        logger.info { "HTTP server started on port ${props.getProperty("FRONTEND_PORT")}" }
      }

    return vertx
      .createHttpServer(
        HttpServerOptions()
          .setRegisterWebSocketWriteHandlers(true)
      )
      .requestHandler(mainRouter)
      .listen(props.getProperty("BACKEND_PORT").toInt()).onFailure { error ->
        logger.error { "Failed to start HTTP server: $error" }
        vertx.eventBus().sendError(Exception("Failed to start the HTTP server"))
      }.onSuccess { http ->
        logger.info { "HTTP server started on port ${props.getProperty("BACKEND_PORT")}" }
      }
  }
}
