package com.ex_dock.ex_dock

import com.ex_dock.ex_dock.backend.enableBackendRouter
import com.ex_dock.ex_dock.database.service.ServerStartException
import com.ex_dock.ex_dock.frontend.account.router.initAccount
import com.ex_dock.ex_dock.frontend.category.router.initCategory
import com.ex_dock.ex_dock.frontend.checkout.router.initCheckout
import com.ex_dock.ex_dock.frontend.home.router.initHome
import com.ex_dock.ex_dock.frontend.product.router.initProduct
import com.ex_dock.ex_dock.frontend.text_pages.router.initTextPages
import com.ex_dock.ex_dock.helper.registerGenericCodec
import com.ex_dock.ex_dock.helper.sendError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.CookieSameSite
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.SessionStore
import java.util.Properties

class MainVerticle : AbstractVerticle() {
  companion object {
    val logger = KotlinLogging.logger {}
    var areCodecsRegistered = false
  }
  private val deployedVerticleIds = emptyList<String>().toMutableList()

  private val props : Properties = javaClass.classLoader.getResourceAsStream("secret.properties").use {
    Properties().apply { load(it) }
  }

  /**
  * This function is the entry point for the Vert.x application. It starts an HTTP server and listens on port 8888.
  *
  * @param startPromise A [Promise] that will be completed when the HTTP server has started successfully or failed to start.
  *
  * @return Nothing is returned from this function.
  */
  override fun start(startPromise: Promise<Void>) {
    vertx.deployVerticle(ExtensionsLauncher())
      .onSuccess{ verticleId ->
        logger.info { "MainVerticle started successfully" }
        deployedVerticleIds.add(verticleId)
      }
      .onFailure { err ->
        logger.error { "Failed to start MainVerticle: $err" }
        startPromise.fail(err)
      }

    val eventBus = vertx.eventBus()
    val mainRouter : Router = Router.router(vertx)
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

    mainRouter.enableBackendRouter(vertx, logger)

    mainRouter.initHome(eventBus)
    mainRouter.initProduct(vertx)
    mainRouter.initCategory(vertx)
    mainRouter.initTextPages(vertx)
    mainRouter.initCheckout(vertx)
    mainRouter.initAccount(vertx)

    vertx
      .createHttpServer(
        HttpServerOptions()
          .setRegisterWebSocketWriteHandlers(true)
      )
      .requestHandler(mainRouter)
      .listen(props.getProperty("FRONTEND_PORT").toInt()) { http ->
        if (http.succeeded()) {
          logger.info { "HTTP server started on port ${props.getProperty("FRONTEND_PORT")}" }
          startPromise.complete()
        } else {
          logger.error { "Failed to start HTTP server: ${http.cause()}" }
          vertx.eventBus().sendError(ServerStartException("Failed to start the HTTP server"))
          startPromise.fail(http.cause())
        }
      }
  }
}
