package com.ex_dock.ex_dock.backend.v1.router.docker

import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.websocket.setAuthTimer
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import oshi.SystemInfo
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

fun Router.initDocker(vertx: Vertx, logger: KLogger, absoluteMounting: Boolean = false) {
  val dockerRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val authTimeOutMillis = 10000L // 10 seconds timeout
  var serverHealth = ServerHealth.DOWN

  fun openDockerData(webSocket: ServerWebSocket) {
    val systemInfo = SystemInfo()
    val hardware = systemInfo.hardware
    val processor = hardware.processor
    val os = systemInfo.operatingSystem

    var prevTicks: LongArray? = null

    val delayMillis = 2000L

    val timerId = vertx.setPeriodic(delayMillis) { it ->
      if (webSocket.isClosed) {
        vertx.cancelTimer(it)
        logger.info { "Canceled CPU data timer for closed websocket" }
        return@setPeriodic
      }

      try {
        val currentPid = os.processId
        val osProcess = os.getProcess(currentPid)
        val processCpuLoad = osProcess?.let { process ->
          process.processCpuLoadCumulative / processor.logicalProcessorCount
        } ?: 0.0

        val totalMemory = hardware.memory.total
        val availableMemory = hardware.memory.available
        val usedMemory = totalMemory - availableMemory

        val currentTicks = processor.systemCpuLoadTicks
        var systemCpuLoad = 0.0

        if (prevTicks != null) {
          systemCpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks)
        }
        prevTicks = currentTicks

        val cpuData = CpuUsage(
          type = "serverStatus",
          timeStamp = System.currentTimeMillis(),
          processCpuLoad = processCpuLoad,
          systemCpuLoad = systemCpuLoad,
          serverHealth = serverHealth,
          totalMemory = BigDecimal(totalMemory / 1024 / 1024).setScale(2).toLong(),
          usedMemory = BigDecimal(usedMemory / 1024 / 1024).setScale(2).toLong(),
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

  eventBus.consumer<ServerHealth>("process.docker.serverHealth").handler { message ->
    serverHealth = message.body()

    if (serverHealth == ServerHealth.RESTARTING) {
      eventBus.send("process.main.redeployVerticles", "")
    }

    message.reply("Success")
  }


  this.route(
    if (absoluteMounting) "$apiMountingPath/v1/docker*" else "/v1/docker*"
  ).subRouter(dockerRouter)
}

@Serializable
data class CpuUsage(
  val type: String,
  val timeStamp: Long,
  val processCpuLoad: Double,
  val systemCpuLoad: Double,
  val totalMemory: Long,
  val usedMemory: Long,
  val serverHealth: ServerHealth
)

enum class ServerHealth(val status: String) {
  UP("UP"),
  DOWN("DOWN"),
  MAINTENANCE("MAINTENANCE"),
  RESTARTING("RESTARTING"),
}
