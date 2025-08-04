package com.ex_dock.ex_dock.helper

import com.ex_dock.ex_dock.MainVerticle
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

fun Properties.load(isDefault: Boolean = false): Properties {
  val configFileName: String = if (isDefault) {
    "default.properties"
  } else {
    "secret.properties"
  }
  val externalConfigPath = "/app/config/$configFileName"
  val localExternalConfigPath = "config/$configFileName"
  val configFile: File

  val potentialExternalFile = File(externalConfigPath)
  if (potentialExternalFile.exists()) {
    configFile = potentialExternalFile
    try {
      FileInputStream(configFile).use { this.load(it) }
    } catch (e: IOException) {
      MainVerticle.logger.error { e.message }
    }
  } else {
    configFile = File(localExternalConfigPath)
    try {
      FileInputStream(configFile).use { this.load(it) }
    } catch (e: IOException) {
      MainVerticle.logger.error { e.message }
    }
  }

  return this
}
