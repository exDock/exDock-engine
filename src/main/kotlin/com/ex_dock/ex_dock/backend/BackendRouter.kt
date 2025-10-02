package com.ex_dock.ex_dock.backend

import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.backend.v1.router.auth.enableAuthRouter
import com.ex_dock.ex_dock.backend.v1.router.docker.initDocker
import com.ex_dock.ex_dock.backend.v1.router.enableBackendV1Router
import com.ex_dock.ex_dock.backend.v1.router.image.initOpenImageRouter
import com.ex_dock.ex_dock.backend.v1.router.websocket.initWebsocket
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpMethod
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler

fun Router.enableBackendRouter(vertx: Vertx, logger: KLogger) {
  val backendRouter: Router = Router.router(vertx)
  val pairDeliveryOptions = DeliveryOptions().setCodecName("PairCodec")
  val eventBus = vertx.eventBus()
  val authProvider = AuthProvider()
  val jwtAuth = JWTAuth.create(
    vertx,
    JWTAuthOptions()
      .addPubSecKey(
        PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setBuffer(authProvider.publicKey)
      )
      .addPubSecKey(
        PubSecKeyOptions()
          .setAlgorithm("RS256")
          .setBuffer(authProvider.privateKey)
      )
  )

  // To avoid a race condition with the JDBC starter
  eventBus.registerGenericCodec(Pair::class)

  eventBus.send(
    "process.authentication.registerKeys",
    Pair(authProvider.privateKey, authProvider.publicKey),
    pairDeliveryOptions
  )

  backendRouter.route().handler(CorsHandler.create()
    .allowedMethod(HttpMethod.OPTIONS)
    .allowedMethod(HttpMethod.GET)
    .allowedMethod(HttpMethod.POST)
    .allowedMethod(HttpMethod.PUT)
    .allowedMethod(HttpMethod.DELETE)
    .allowedMethod(HttpMethod.PATCH)
  )

  backendRouter.enableAuthRouter(vertx)

  // Only use these routers for websockets, because they use other authorization methods
  backendRouter.initWebsocket(vertx, logger = logger)
  backendRouter.initDocker(vertx, logger = logger)
  backendRouter.initOpenImageRouter(vertx)

  backendRouter.route().handler(JWTAuthHandler.create(jwtAuth))

  backendRouter.get("/about").handler { ctx ->
    ctx.end("about page for the APIs")
  }

  backendRouter.enableBackendV1Router(vertx)

  this.route("$apiMountingPath*").subRouter(backendRouter)
}
