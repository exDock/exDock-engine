package com.ex_dock.ex_dock.helper

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

fun Properties.load(): Properties {
  val configFileName = "secret.properties"
  val externalConfigPath = "/app/config/$configFileName"
  val localExternalConfigPath = "config/$configFileName"
  val configFile: File

  val potentialExternalFile = File(externalConfigPath)
  if (potentialExternalFile.exists()) {
    configFile = potentialExternalFile
    try {
      FileInputStream(configFile).use { this.load(it) }
    } catch (_: IOException) {}
  } else {
    configFile = File(localExternalConfigPath)
    try {
      FileInputStream(configFile).use { this.load(it) }
    } catch (_: IOException) {}
  }

  return this
}
