package com.ex_dock.ex_dock.backend.v1.router.websocket

import com.ex_dock.ex_dock.backend.apiMountingPath
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.concurrent.ConcurrentHashMap

fun Router.initWebsocket(vertx: Vertx, absoluteMounting: Boolean = false) {
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val websocketRouter = Router.router(vertx)

  websocketRouter["/test"].handler { ctx ->
    ctx.end("Got request")
  }

  websocketRouter.route("/error").handler { ctx ->
    ctx.request().toWebSocket { asyncResult ->
      if (asyncResult.succeeded()) {
        val webSocket = asyncResult.result()
        val clientId = webSocket.binaryHandlerID()
        val userId = ctx.user().principal().getString("userId")
        connectedClients[clientId] = webSocket
        userIdToConnectionId[userId] = clientId

        webSocket.handler { buffer ->
          val message = buffer.toString()
          println("Received: $message")
          webSocket.writeTextMessage("Server received: $message")
        }

        webSocket.closeHandler { _ ->
          println("Client disconnected: $clientId")
          connectedClients.remove(clientId)
          userIdToConnectionId.remove(userId)
        }
        webSocket.exceptionHandler { error ->
          println("Error: ${error.message}")
          connectedClients.remove(clientId)
          userIdToConnectionId.remove(userId)
        }
      } else {
        ctx.response().setStatusCode(400).end()
      }
    }
  }

  vertx.eventBus().consumer<JsonObject>("process.websocket.broadcastError").handler { message ->
    val errorEvent = message.body()
    val targetType = errorEvent.getString("targetType", "BROADCAST")
    val targetIdentifier = errorEvent.getString("targetIdentifier", "")
    val errorPayload = errorEvent.getJsonObject("errorPayload", null)

    if (errorPayload == null) {
      println("message missing error payload")
    }

    val wsErrorMessage = JsonObject()
      .put("type", "errorNotification")
      .put("error", errorPayload)

    val wsMessageString = wsErrorMessage.encode()

    when (targetType.uppercase()) {
      "CONNECTION_ID" -> {
        if (targetIdentifier != "") {
          val connectionId = userIdToConnectionId[targetIdentifier]
          val clientSocket = connectedClients[connectionId]
          if (clientSocket != null) {
            try {
              if (!clientSocket.isClosed) {
                clientSocket.writeTextMessage(wsMessageString)
              }
            } catch (e: Exception) {
              println(e.message)
            }
          } else {
            println("Client not in connection pool")
          }
        }
      }
      "BROADCAST" -> {
        if (connectedClients.isEmpty()) return@handler

        for (entry in connectedClients.entries) {
          try {
            if (!entry.value.isClosed) {
              entry.value.writeTextMessage(wsMessageString)
            }
          } catch (e: Exception) {
            println("error")
          }
        }
      }
    }
  }

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1/ws*" else "/v1/ws*"
  ).subRouter(websocketRouter)
}
