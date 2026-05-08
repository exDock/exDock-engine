package com.ex_dock.ex_dock.frontend.router

import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.database.url.UrlKeys
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler

fun Router.enableFrontendRouter(vertx: Vertx, logger: KLogger, authProvider: AuthProvider) {
  val frontendRouter: Router = Router.router(vertx)
  val pairDeliveryOptions = DeliveryOptions().setCodecName("PairCodec")
  val eventBus = vertx.eventBus()

  eventBus.send(
    "process.authentication.registerKeys",
    Pair(authProvider.privateKey, authProvider.publicKey),
    pairDeliveryOptions
  )

  frontendRouter.route().handler(
    CorsHandler.create()
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
  )

  frontendRouter.get("/about").handler { ctx ->
    ctx.end("about page for the Frontend")
  }

  frontendRouter["/*"].handler { ctx ->
    val params = ctx.queryParams()
    val fullUrl = ctx.request().path()

    eventBus.request<JsonObject>("process.url.getUrlByKey", fullUrl).onFailure {
      ctx.fail(404, Error("Page not found"))
      return@onFailure
    }.onSuccess { fetchedUrl ->
      val urlObject: UrlKeys = UrlKeys.fromJson(fetchedUrl.body())
      val request = JsonObject()

      for (param in urlObject.requiredParameters) {
        request.put(param, params.get(param))
        if (!params.contains(param)) {
          ctx.fail(400, Error("Missing required parameter: $param"))
          return@onSuccess
        }
      }

      val futures = mutableListOf<Future<Unit>>()
      var pageCode = ""
      urlObject.templates.forEach { templateId ->
        futures.add(
          Future.future { promise ->
            val newBody = JsonObject(request.toString()).put("template_key", templateId)
            eventBus.request<JsonObject>("template.generate.compiled", newBody).onFailure {
              promise.fail(it.message)
            }.onSuccess {
              pageCode += it.body()
              promise.complete()
            }
          }
        )
      }

      Future.all<Unit>(futures).onFailure {
        ctx.fail(500, it)
      }.onSuccess { _ ->
        ctx.response()
          .putHeader("Content-Type", "text/html")
          .end(pageCode)
      }
    }
  }

  this.route().subRouter(frontendRouter)
}
