package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.ThreadingModel
import io.vertx.core.Vertx
import io.vertx.core.DeploymentOptions
import kotlin.reflect.KClass

class VerticleDeployHelper {
  companion object {
    val logger = KotlinLogging.logger {}
  }
}


fun deployVerticleHelper(vertx: Vertx, name: String): Future<String> {
  val promise: Promise<String> = Promise.promise()
  vertx.deployVerticle(name)
    .onComplete{ res ->
      if (res.failed()) {
        MainVerticle.logger.error { "Failed to deploy Verticle: $name\nCause: ${res.cause()}" }
        promise.fail(res.cause())
      } else {
        MainVerticle.logger.info { "Verticle deployed successfully: $name" }
        promise.complete(res.result())
      }
    }

  return promise.future()
}

fun Vertx.deployVerticleHelper(name: KClass<*>): Future<String> {
  return deployVerticleHelper(this, name.qualifiedName.toString())
}


fun deployWorkerVerticleHelper(vertx: Vertx, name: String, workerPoolSize: Int, instances: Int): Future<String> {
  val promise: Promise<String> = Promise.promise()
  val options: DeploymentOptions = DeploymentOptions()
    .setThreadingModel(ThreadingModel.WORKER)
    .setWorkerPoolName(name)
    .setWorkerPoolSize(workerPoolSize)
    .setInstances(instances)

  vertx.deployVerticle(name, options)
    .onComplete{ res ->
      if (res.failed()) {
        var stackTrace = ""
        for (stackTraceElement in res.cause().stackTrace) {
          stackTrace += "\t$stackTraceElement\n"
        }
        MainVerticle.logger.error {
          "Failed to deploy to worker Verticle: $name\nCause: ${res.cause()}\nStacktrace: $stackTrace"
        }
        promise.fail(res.cause())
      } else {
        MainVerticle.logger.info { "Verticle deployed to worker successfully: $name" }
        promise.complete(res.result())
      }
    }

  return promise.future()
}

fun Vertx.deployWorkerVerticleHelper(name: KClass<*>, workerPoolSize: Int = 1, instances: Int = workerPoolSize): Future<String> {
  return deployWorkerVerticleHelper(this, name.qualifiedName.toString(), workerPoolSize, instances)
}


fun deployVirtualVerticleHelper(vertx: Vertx, name: String): Future<Void> {
  val promise: Promise<Void> = Promise.promise<Void>()
  val options: DeploymentOptions = DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
  vertx.deployVerticle(name, options)
    .onComplete{ res ->
      if (res.failed()) {
        MainVerticle.logger.error { "Failed to deploy virtual Verticle: $name\nCause: ${res.cause()}" }
        promise.fail(res.cause())
      } else {
        MainVerticle.logger.info { "Verticle deployed to virtual successfully: $name" }
        promise.complete()
      }
    }

  return promise.future()
}

fun Vertx.deployVirtualVerticleHelper(name: KClass<*>): Future<Void> {
  return deployVirtualVerticleHelper(this, name.qualifiedName.toString())
}
