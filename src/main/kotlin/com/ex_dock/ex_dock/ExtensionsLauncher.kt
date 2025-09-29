package com.ex_dock.ex_dock

import com.ex_dock.ex_dock.database.JDBCStarter
import com.ex_dock.ex_dock.frontend.FrontendVerticle
import com.ex_dock.ex_dock.helper.deployVerticleHelper
import com.ex_dock.ex_dock.helper.load
import com.ex_dock.ex_dock.helper.registerVerticleIds
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import java.util.*

class ExtensionsLauncher: VerticleBase() {
  companion object {
    val logger = KotlinLogging.logger {}
  }

  private val verticleIds = emptyList<String>().toMutableList()

  private lateinit var props: Properties

  /**
 * This function is responsible for starting the ExtensionsLauncher verticle.
 * It initializes the necessary properties, checks for available extensions, and deploys them.
 */
override fun start(): Future<*> {
    // Load properties from the 'secret.properties' file
    props = Properties().load()

    // Wait for JDBC Verticle to start first
    return deployVerticleHelper(vertx, JDBCStarter::class.qualifiedName.toString())
      .onFailure{ error ->
        logger.error { "Failed to deploy JDBC verticle: $error" }
      }.onSuccess {  verticleId ->
        verticleIds.add(verticleId)
        vertx.eventBus().registerVerticleIds(verticleIds)

        // Check for available extensions and deploy them
        checkExtensions()
      }
}

  private fun checkExtensions() {
    // ADD frontend Verticles
    vertx.deployVerticleHelper(FrontendVerticle::class).onFailure { error ->
      logger.error { "Failed to deploy frontend verticle: $error" }
    }.onSuccess { verticleId ->
      verticleIds.add(verticleId)
    }
  }
}
