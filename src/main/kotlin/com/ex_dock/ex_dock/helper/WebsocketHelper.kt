package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.backend.v1.router.websocket.setAuthTimer
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import kotlin.collections.set
import kotlin.properties.Delegates

fun Vertx.handleWebSocketAuthentication(
  result: ServerWebSocket,
  logger: KLogger,
  authTimeOutMillis: Long = 10000L,
  connectedClients: MutableMap<String, ServerWebSocket>,
  userIdToConnectionId: MutableMap<String, String>,
  clientPools: MutableMap<String, MutableList<ServerWebSocket>>? = null,
  mainMessageHandler: (String, Buffer) -> Unit
  ) {
  val webSocket = result
  val clientId = webSocket.binaryHandlerID()
  var authenticatedUserId: String? = null
  var timerId by Delegates.notNull<Long>()
  var firstAuthAttempt = true

  logger.info { "Client $clientId attempting to connect..." }

  timerId = this.setAuthTimer(authTimeOutMillis, authenticatedUserId, webSocket)

  webSocket.handler { accessBuffer ->
    this.cancelTimer(timerId)

    try {
      val authMessageJson = accessBuffer.toJsonObject()
      val accessToken = authMessageJson.getString("token")

      this.eventBus().request<String>(
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

          val authenticatedHandler: (Buffer) -> Unit = { buffer ->
            mainMessageHandler(authenticatedUserId, buffer)
          }

          if (clientPools != null) {
            if (!clientPools.containsKey(authenticatedUserId)) {
              clientPools[authenticatedUserId] = mutableListOf()
            }
            clientPools[authenticatedUserId]?.add(webSocket)
          }

          webSocket.handler(authenticatedHandler)
        } else {
          webSocket.writeTextMessage(
            JsonObject()
              .put("type", "auth_failure")
              .put("message", "Authentication failed: User identification error.")
              .encode()
          )
          if (!firstAuthAttempt) {
            logger.info { "Client $clientId failed to authenticate. Closing connection" }
            webSocket.close()
          } else {
            firstAuthAttempt = false
            logger.info { "Client $clientId failed to authenticate." }
            timerId = this.setAuthTimer(authTimeOutMillis, authenticatedUserId, webSocket)
          }
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
      this.cancelTimer(timerId)
      connectedClients.remove(clientId)
      logger.info { "Client $clientId disconnected." }
      if (authenticatedUserId != null) {
        userIdToConnectionId.remove(authenticatedUserId)
      }

      if (clientPools != null) {
        clientPools[authenticatedUserId]?.remove(webSocket)
      }
    }

    webSocket.exceptionHandler { error ->
      this.cancelTimer(timerId)
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
}
