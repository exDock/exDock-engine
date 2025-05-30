package com.ex_dock.ex_dock.backend.v1.router.system

import com.ex_dock.ex_dock.ClassLoaderDummy
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.util.Properties

class SystemVerticle: AbstractVerticle() {
  companion object {
    val logger = KotlinLogging.logger {}
  }

  private lateinit var eventBus: EventBus

  override fun start() {
    eventBus = vertx.eventBus()

    getSystemVariables()
    saveSystemVariables()
  }

  private fun getSystemVariables() {
    eventBus.consumer<Unit>("process.system.getVariables").handler { message ->
      val jsonSettings = JsonObject()
      val settingsMap = JsonArray()
      lateinit var props: Properties
      try {
        props = ClassLoaderDummy::class.java.classLoader.getResourceAsStream("secret.properties").use {
          Properties().apply { load(it) }
        }

        props.generateResponse(message, jsonSettings, settingsMap)
      } catch (_: Exception) {
        logger.warn { "Could not find custom settings using default settings" }
        try {
          props = ClassLoaderDummy::class.java.classLoader.getResourceAsStream("default.properties").use {
            Properties().apply { load(it) }
          }

          props.generateResponse(message, jsonSettings, settingsMap)
        } catch (_: Exception) {
          logger.error { "Could not find default settings" }
          message.fail(400, "Could not find settings")
        }
      }
    }
  }

  private fun saveSystemVariables() {
    eventBus.consumer<JsonObject>("process.system.saveVariables").handler { message ->
      try {
        val props = ClassLoaderDummy::class.java.classLoader.getResourceAsStream("secret.properties").use {
          Properties().apply { load(it) }
        }

        props.entries.forEach { mutableEntry ->
          props.setProperty(mutableEntry.key.toString(), message.body().getValue("${mutableEntry.key}") as String)
        }

        val path = ClassLoaderDummy::class.java.classLoader.getResource("secret.properties")
        if (path == null) {
          logger.error { "Could not find secret.properties" }
          message.fail(400, "Could not find secret.properties")
        }
        message.reply("test")
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
