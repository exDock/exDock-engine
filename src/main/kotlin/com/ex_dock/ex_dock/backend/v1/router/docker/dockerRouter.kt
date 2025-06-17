package com.ex_dock.ex_dock.backend.v1.router.docker

import com.ex_dock.ex_dock.ClassLoaderDummy
import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.websocket.initWebsocket
import com.ex_dock.ex_dock.backend.v1.router.websocket.setAuthTimer
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.model.Statistics
import com.github.dockerjava.core.DockerClientBuilder
import io.github.oshai.kotlinlogging.KLogger
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

fun Router.initDocker(vertx: Vertx, logger: KLogger, absoluteMounting: Boolean = false) {
  val dockerRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()
  var isDockerLoaded: Boolean = false
  val connectedClients = ConcurrentHashMap<String, ServerWebSocket>()
  val userIdToConnectionId = ConcurrentHashMap<String, String>()
  val authTimeOutMillis = 10000L // 10 seconds timeout

  lateinit var dockerContainerId: String
  lateinit var dockerClient: DockerClient

  try {
    val props = ClassLoaderDummy::class.java.classLoader.getResourceAsStream("secret.properties").use {
      Properties().apply { load(it) }
    }

    dockerContainerId = props.getProperty("DOCKER_ID")
    isDockerLoaded = true
  } catch (_: Exception) {}

  fun openDockerData(webSocket: ServerWebSocket) {

    val statsCmd = dockerClient.statsCmd(dockerContainerId)

    try {
      statsCmd.exec(object : Adapter<Statistics>() {
        override fun onNext(stats: Statistics) {
          val cpuData = (stats.cpuStats.cpuUsage?.totalUsage ?: 0) - (stats.preCpuStats?.cpuUsage?.totalUsage ?: 0)
          val systemDelta = (stats.cpuStats?.systemCpuUsage ?: 0) - (stats.preCpuStats?.systemCpuUsage ?: 0)
          val numberOfCores = stats.cpuStats?.onlineCpus ?: 0

          if (systemDelta > 0 && numberOfCores > 0) {
            val cpuPercentage = (cpuData.toDouble() / systemDelta) * numberOfCores * 100.0
            val jsonObject = JsonObject()
              .put("CPU_delta", cpuData)
              .put("SYSTEM_delta", systemDelta)
              .put("number_of_cores", numberOfCores)
              .put("CPU_percentage", cpuPercentage)
            webSocket.writeTextMessage(jsonObject.encode())
          } else {
            webSocket.writeTextMessage("Could not calculate")
          }
        }

        override fun onError(throwable: Throwable) {
          logger.error { "Error getting docker data: ${throwable.message}" }
        }

        override fun onComplete() {
          logger.info { "Docker stats stream completed" }
        }
      }).awaitCompletion()
    } catch (e: Exception) {
      logger.error { "Error getting docker data: ${e.message}" }
    } finally {
      dockerClient.close()
    }
  }

  dockerRouter.route("/getData").handler { ctx ->
    ctx.request().toWebSocket().onSuccess { result ->
      val webSocket = result
      val clientId = webSocket.binaryHandlerID()
      var authenticatedUserId: String? = null
      var timerId by Delegates.notNull<Long>()
      var firstAuthAttempt = true

      if (!isDockerLoaded) {
        webSocket.writeTextMessage("Docker client not loaded")
        webSocket.close()
      } else {
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

                if (connectedClients.isEmpty()) {
                  dockerClient = DockerClientBuilder.getInstance().build()
                }

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

            if (connectedClients.isEmpty()) {
              dockerClient.close()
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
