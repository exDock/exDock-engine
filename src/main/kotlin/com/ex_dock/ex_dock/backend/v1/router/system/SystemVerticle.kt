package com.ex_dock.ex_dock.backend.v1.router.system

import com.ex_dock.ex_dock.helper.load
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.util.*

class SystemVerticle: VerticleBase() {
  companion object {
    val logger = KotlinLogging.logger {}
  }

  private lateinit var eventBus: EventBus

  override fun start(): Future<*>? {
    eventBus = vertx.eventBus()

    getSystemVariables()
    saveSystemVariables()

    return Future.succeededFuture<Unit>()
  }

  private fun getSystemVariables() {
    eventBus.consumer<Unit>("process.system.getVariables").handler { message ->
      val jsonSettings = JsonObject()
      val settingsMap = JsonArray()
      val props = Properties().load()

      props.generateResponse(message, jsonSettings, settingsMap)
    }
  }

  private fun saveSystemVariables() {
    eventBus.consumer<JsonObject>("process.system.saveVariables").handler { message ->
      try {
        val props = Properties().load()

        props.entries.forEach { mutableEntry ->
          if (message.body().containsKey(mutableEntry.key.toString())) {
            props.setProperty(mutableEntry.key.toString(), message.body().getValue("${mutableEntry.key}") as String)
          }
        }

        val externalPath = "/app/config/secret.properties"
        val localPath = "config/secret.properties"
        val path = if (File(externalPath).exists()) externalPath else localPath

        props.store(File(path).outputStream(), null)

        message.reply("Successfully saved settings")
      } catch (_: Exception) {
        logger.error { "Could not find settings" }
        message.fail(400, "Could not find settings")
      }
    }
  }
}


private fun JsonArray.addSettingAttribute(id: String, name: String, type: String, value: Any) {
  val setting = JsonObject()
    .put("attribute_id", id)
    .put("attribute_name", name.encode())
    .put("attribute_type", type.toBackOfficeType(id))
    .put("current_attribute_value", value)
  this.add(setting)
}

private fun Properties.generateResponse(message: Message<Unit>, jsonSettings: JsonObject, settingsMap: JsonArray) {
  this.entries.forEach { entry ->
    val key = entry.key
    val value = entry.value
    settingsMap.addSettingAttribute(key.toString(), key.toString(), value::class.java.simpleName, value.toString())
  }

  jsonSettings
    .put("block_type", "standard")
    .put("attributes", settingsMap)

  message.reply(JsonObject()
    .put("Backend Settings", jsonSettings).encodePrettily())
}

private fun String.encode(): String {
  var result = this.lowercase()
  result = result.replace("_", " ")
  result = result.replaceFirstChar { it.uppercase() }
  return result
}

private fun String.toBackOfficeType(name: String): String {
  if (name.contains("PASSWORD", ignoreCase = true)) return "password"

  return when (this) {
    "Int" -> "int"
    else -> "text"
  }
}
