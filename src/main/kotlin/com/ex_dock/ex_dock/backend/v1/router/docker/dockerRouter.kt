package com.ex_dock.ex_dock.backend.v1.router.docker

import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.websocket.setAuthTimer
import com.sun.management.OperatingSystemMXBean
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.management.ManagementFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

fun Router.initDocker(vertx: Vertx, logger: KLogger, absoluteMounting: Boolean = false) {
  val dockerRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val authTimeOutMillis = 10000L // 10 seconds timeout

  fun openDockerData(webSocket: ServerWebSocket) {
    val osBean = ManagementFactory.getOperatingSystemMXBean() as? OperatingSystemMXBean

    if (osBean == null) {
      webSocket.close()
    }

    val delayMilis = 2000L

    val timerId = vertx.setPeriodic(delayMilis) {
      if (webSocket.isClosed) {
        vertx.cancelTimer(it)
        logger.info { "Canceled CPU data timer for closed websocket" }
        return@setPeriodic
      }

      try {
        val processCpuLoad = osBean?.processCpuLoad
        val systemCpuLoad = osBean?.cpuLoad

        val cpuData = CpuUsage(System.currentTimeMillis(),
          processCpuLoad?.times(100) ?: 0.0,
          systemCpuLoad?.times(100) ?: 0.0,
        )

        val jsonMessage = Json.encodeToString(CpuUsage.serializer(), cpuData)

        webSocket.writeTextMessage(jsonMessage)
      } catch (e: Exception) {
        webSocket.close()
        logger.error { e.message }
      }
    }

    webSocket.closeHandler {
      vertx.cancelTimer(timerId)
      logger.info { "Canceled CPU data timer for closed websocket" }

      val clientId = webSocket.binaryHandlerID()
      connectedClients.remove(clientId)
      if (userIdToConnectionId.containsValue(clientId)) {
        userIdToConnectionId.entries.find { it.value == clientId }?.key?.let { userIdToConnectionId.remove(it) }
      }
    }
  }

  dockerRouter.route("/getData").handler { ctx ->
    ctx.request().toWebSocket().onSuccess { result ->
      val webSocket = result
      val clientId = webSocket.binaryHandlerID()
      var authenticatedUserId: String? = null
      var timerId by Delegates.notNull<Long>()
      var firstAuthAttempt = true

        timerId = vertx.setAuthTimer(authTimeOutMillis, authenticatedUserId, webSocket)

        val mainMessageHandler: (Buffer) -> Unit = { buffer ->
          val message = buffer.toString()

          webSocket.writeTextMessage("Server received: $message")
        }

        webSocket.handler { accessBuffer ->
          vertx.cancelTimer(timerId)

          try {
            val authMessageJson = accessBuffer.toJsonObject()
            val accessToken = authMessageJson.getString("token")

            vertx.eventBus().request<String>(
              "process.authentication.authenticateToken", accessToken
            ).onComplete { result ->
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
                openDockerData(webSocket)
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
                  timerId = vertx.setAuthTimer(authTimeOutMillis, authenticatedUserId, webSocket)
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
    }.onFailure { _ ->
      logger.error { "Failed to upgrade to WebSocket" }
      ctx.response().setStatusCode(400).end("Failed to upgrade to WebSocket")
    }
  }


  this.route(
    if (absoluteMounting) "$apiMountingPath/v1/docker*" else "/v1/docker*"
  ).subRouter(dockerRouter)
}

@Serializable
data class CpuUsage(
  val timeStamp: Long,
  val processCpuLoad: Double,
  val systemCpuLoad: Double,
)

enum class ServerHealth(val status: String) {
  UP("UP"),
  DOWN("DOWN"),
  MAINTENANCE("MAINTENANCE"),
  RESTARTING("RESTARTING"),
}
