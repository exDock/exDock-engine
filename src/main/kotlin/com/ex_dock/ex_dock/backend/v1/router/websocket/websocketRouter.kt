package com.ex_dock.ex_dock.backend.v1.router.websocket

import com.ex_dock.ex_dock.backend.apiMountingPath
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.concurrent.ConcurrentHashMap

fun Router.initWebsocket(vertx: Vertx, absoluteMounting: Boolean = false, logger: KLogger) {
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val websocketRouter = Router.router(vertx)
  val authTimeOutMillis: Long = 10000L // 10 seconds timeout

  websocketRouter["/test"].handler { ctx ->
    ctx.end("Got request")
  }

  websocketRouter.route("/error").handler { ctx ->
    ctx.request().toWebSocket { asyncResult ->
//      if (asyncResult.succeeded()) {
//        val webSocket = asyncResult.result()
//        val clientId = webSocket.binaryHandlerID()
//        val userId = ctx.user().principal().getString("userId")
//        connectedClients[clientId] = webSocket
//        userIdToConnectionId[userId] = clientId
//
//        webSocket.handler { buffer ->
//          val message = buffer.toString()
//          println("Received: $message")
//          webSocket.writeTextMessage("Server received: $message")
//        }
//
//        webSocket.closeHandler { _ ->
//          println("Client disconnected: $clientId")
//          connectedClients.remove(clientId)
//          userIdToConnectionId.remove(userId)
//        }
//        webSocket.exceptionHandler { error ->
//          println("Error: ${error.message}")
//          connectedClients.remove(clientId)
//          userIdToConnectionId.remove(userId)
//        }
//      } else {
//        ctx.response().setStatusCode(400).end()
//      }
      if (asyncResult.succeeded()) {
        val webSocket = asyncResult.result()
        val clientId = webSocket.binaryHandlerID()
        var authenticatedUserId: String? = null
        var timerId: Long = -1

        logger.info { "Client $clientId attempting to connect..." }

        timerId = vertx.setTimer(authTimeOutMillis) {
          if (authenticatedUserId == null && !webSocket.isClosed) {
            logger.info("Client $clientId timed out")
            webSocket.writeTextMessage(
              JsonObject()
                .put("type", "auth_timeout")
                .put("message", "Authentication timed out. Closing connection.")
                .encode()
            )
            webSocket.close()
          }
        }

        val mainMessageHandler: (Buffer) -> Unit = { buffer ->
          val message = buffer.toString()
          logger.info { "Authenticated client $clientId (User: $authenticatedUserId) received: $message" }
          webSocket.writeTextMessage("Server received: $message")
        }

        webSocket.handler { accessBuffer ->
          vertx.cancelTimer(timerId)

          try {
            val authMessageJson = accessBuffer.toJsonObject()
            val accessToken = authMessageJson.getString("token")

            vertx.eventBus().request<String>(
              "process.authentication.authenticateToken", accessToken).onComplete { result ->
              if (result.succeeded()) {
                val userIdFromAuth = result.result().body()
                authenticatedUserId = userIdFromAuth

                userIdToConnectionId[userIdFromAuth] = clientId
                connectedClients[userIdFromAuth] = webSocket

                logger.info { "Client $clientId authenticated successfully as user $authenticatedUserId." }
                webSocket.writeTextMessage(
                  JsonObject()
                    .put("type", "auth_success")
                    .put("message", "Authentication successful.")
                    .encode()
                )

                webSocket.handler(mainMessageHandler)
              } else {
                logger.info { "Client $clientId failed to authenticate." }
                webSocket.writeTextMessage(
                  JsonObject()
                    .put("type", "auth_failure")
                    .put("message", "Authentication failed: User identification error.")
                    .encode()
                )
              }
            }
          } catch (e: Exception) {
            logger.error { "Client $clientId sent invalid auth message format: ${e.message}" }
            webSocket.writeTextMessage(
              JsonObject()
                .put("type", "auth_failure")
                .put("message", "Invalid authentication message format.")
                .encode()
            )
            webSocket.close()
          }

          webSocket.closeHandler { _ ->
            vertx.cancelTimer(timerId)
            connectedClients.remove(clientId)
            logger.info { "Client $clientId disconnected." }
            if (authenticatedUserId != null) {
              userIdToConnectionId.remove(authenticatedUserId)
            }
          }

          webSocket.exceptionHandler { error ->
            vertx.cancelTimer(timerId)
            connectedClients.remove(clientId)
            logger.error { "Error for client $clientId (User: $authenticatedUserId): ${error.message}" }
            if (authenticatedUserId != null) {
              userIdToConnectionId.remove(authenticatedUserId)
            }
            if (!webSocket.isClosed) {
              webSocket.close()
            }
          }
        }
      } else {
        logger.error { "Failed to upgrade to WebSocket" }
        ctx.response().setStatusCode(400).end("Failed to upgrade to WebSocket")
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
