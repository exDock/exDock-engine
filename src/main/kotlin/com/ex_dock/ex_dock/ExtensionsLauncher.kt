package com.ex_dock.ex_dock

import com.ex_dock.ex_dock.database.JDBCStarter
import com.ex_dock.ex_dock.frontend.FrontendVerticle
import com.ex_dock.ex_dock.helper.deployVerticleHelper
import com.ex_dock.ex_dock.helper.registerVerticleIds
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.ext.web.client.WebClient
import java.util.*

class ExtensionsLauncher: AbstractVerticle() {
  companion object {
    val logger = KotlinLogging.logger {}
  }

  private val verticleIds = emptyList<String>().toMutableList()

  private lateinit var props: Properties

  /**
 * This function is responsible for starting the ExtensionsLauncher verticle.
 * It initializes the necessary properties, checks for available extensions, and deploys them.
 *
 * @param startPromise A Promise object that will be completed when the verticle has started successfully or failed.
 */
override fun start(startPromise: Promise<Void>) {
    // Load properties from the 'secret.properties' file
    props = javaClass.classLoader.getResourceAsStream("secret.properties").use {
      Properties().apply { load(it) }
    }

    // Check for available extensions and deploy them
    checkExtensions()

    // Wait for JDBC Verticle to start first
    deployVerticleHelper(vertx, JDBCStarter::class.qualifiedName.toString())
      .onFailure{ error ->
        logger.error { "Failed to deploy JDBC verticle: $error" }
        startPromise.fail(error)
      }.onSuccess {  verticleId ->
        verticleIds.add(verticleId)
        vertx.eventBus().registerVerticleIds(verticleIds)
      }
}

  private fun checkExtensions() {
    val client = WebClient.create(vertx)

    // ADD frontend Verticles
    vertx.deployVerticleHelper(FrontendVerticle::class).onFailure { error ->
      logger.error { "Failed to deploy frontend verticle: $error" }
    }.onSuccess { verticleId ->
      verticleIds.add(verticleId)
    }
  }
}
