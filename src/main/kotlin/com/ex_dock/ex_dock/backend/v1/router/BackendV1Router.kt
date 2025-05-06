package com.ex_dock.ex_dock.backend.v1.router

import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler


fun Router.enableBackendV1Router(vertx: Vertx, absoluteMounting: Boolean = false) {
  val backendV1Router: Router = Router.router(vertx)
  val authProvider = AuthProvider()
  val exDockAuthHandler = ExDockAuthHandler(vertx)
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

  backendV1Router.route().handler(BodyHandler.create())

//  backendV1Router.post("/token").handler { ctx ->
//    val requestBody = ctx.body().asJsonObject()
//    val credentials = UsernamePasswordCredentials(
//      requestBody.getString("email"),
//      requestBody.getString("password")
//    )
//
//    exDockAuthHandler.authenticate(credentials) {
//      if (it.succeeded()) {
//        val user = it.result()
//        val token = jwtAuth.generateToken(
//          JsonObject().apply {
//            put("userId", user.principal().getString("id"))
//            put("email", user.principal().getString("email"))
//            put("authorizations", user.principal().getJsonArray("authorizations"))
//          },
//          JWTOptions().setAlgorithm("RS256")
//        )
//
//        ctx.response().putHeader("Content-Type", "text/plain").end(token)
//      } else {
//        ctx.response().setStatusCode(401).end("Authentication failed")
//      }
//    }
//  }

  backendV1Router["/test"].handler { ctx ->
    val token: String = ctx.request().headers()["Authorization"].replace("Bearer ", "")
    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
      if (it.getBoolean("success")) {
        ctx.end()
      } else {
        ctx.response().setStatusCode(403).end("User does not have the permission for this")
      }
    }
  }

  // TODO: routing

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1*" else "/v1*"
  ).subRouter(backendV1Router)
}
