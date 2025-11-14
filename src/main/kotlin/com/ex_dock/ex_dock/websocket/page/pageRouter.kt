package com.ex_dock.ex_dock.websocket.page

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.helper.handleWebSocketAuthentication
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.concurrent.ConcurrentHashMap

fun Router.enableWebsocketPageRouter(vertx: Vertx, logger: KLogger) {
  val websocketPageRouter = Router.router(vertx)
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val clientPools = ConcurrentHashMap<String, MutableList<ServerWebSocket>>()
  val eventBus = vertx.eventBus()

  websocketPageRouter["/preview"].handler { ctx ->
    ctx.request().toWebSocket().onSuccess { result ->
      vertx.handleWebSocketAuthentication(
        result = result,
        logger = logger,
        connectedClients = connectedClients,
        userIdToConnectionId = userIdToConnectionId,
        clientPools = clientPools,
      ) { authenticatedUserId, buffer ->
        val websocket = connectedClients[authenticatedUserId]
        val message = buffer.toString()
        val futures = mutableListOf<Future<Unit>>()
        val body = JsonObject(message)
        val templateIds = body.getJsonArray("templateIds")
        var pageCode = ""

        templateIds.forEach { templateId ->
          futures.add(
            Future.future { promise ->
              eventBus.request<JsonObject>("process.template.getTemplateByKey", templateId.toString()).onFailure {
                promise.fail(it.message)
              }.onSuccess {
                pageCode += it.body().getString("template_data")
                promise.complete()
              }
            }
          )
        }

        Future.all<Unit>(futures).onFailure {
          websocket?.writeTextMessage(it.message)
        }.onSuccess { _ ->
          body.put("templateData", pageCode)
          body.remove("templateIds")

          eventBus.request<String>("template.generate.singleUse", body).onFailure {
            MainVerticle.logger.error { it.localizedMessage }
            websocket?.writeTextMessage(it.message)
          }.onSuccess {
            for (ws in clientPools[authenticatedUserId]!!) {
              ws.writeTextMessage(JsonObject()
                .put("type", "pagePreview")
                .put("previewCode", it.body())
                .encode())
            }
          }
        }
      }
    }.onFailure { _ ->
      logger.error { "Failed to upgrade to WebSocket" }
      ctx.response().setStatusCode(400).end("Failed to upgrade to WebSocket")
    }
  }

  this.route("/page*").subRouter(websocketPageRouter)
}
