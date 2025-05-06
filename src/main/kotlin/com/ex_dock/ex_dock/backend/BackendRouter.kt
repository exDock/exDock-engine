package com.ex_dock.ex_dock.backend

import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.backend.v1.router.auth.enableAuthRouter
import com.ex_dock.ex_dock.backend.v1.router.enableBackendV1Router
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler

fun Router.enableBackendRouter(vertx: Vertx) {
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

  eventBus.send(
    "process.authentication.registerKeys",
    Pair(authProvider.privateKey, authProvider.publicKey),
    pairDeliveryOptions
  )

  backendRouter.enableAuthRouter(vertx)

  backendRouter.route().handler(CorsHandler.create())
  backendRouter.route().handler(JWTAuthHandler.create(jwtAuth))

  backendRouter.get("/about").handler { ctx ->
    ctx.end("about page for the APIs")
  }

  backendRouter.enableBackendV1Router(vertx)

  this.route("$apiMountingPath*").subRouter(backendRouter)
}
