package com.ex_dock.ex_dock.database.server

import io.vertx.core.json.JsonObject

data class ServerDataData(
  val key: String,
  var value: String,
  ) {
  companion object {
    fun fromJson(json: JsonObject): ServerDataData {
      val key = json.getString("key")
      val value = json.getString("value")

      return ServerDataData(key, value)
    }
  }

}

data class ServerVersionData(
  val major: Int,
  val minor: Int,
  val patch: Int,
  var versionName: String,
  var versionDescription: String,
  ) {
  companion object {
    fun fromJson(json: JsonObject): ServerVersionData {
      val major = json.getInteger("major")
      val minor = json.getInteger("minor")
      val patch = json.getInteger("patch")
      val versionName = json.getString("version_name")
      val versionDescription = json.getString("version_description")

      return ServerVersionData(major, minor, patch, versionName, versionDescription)
    }
  }
}


fun ServerDataData.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("key", key)
  document.put("value", value)

  return document
}

fun ServerVersionData.toDocument(): JsonObject {
  val document = JsonObject()
  document.put("major", major)
  document.put("minor", minor)
  document.put("patch", patch)
  document.put("version_name", versionName)
  document.put("version_description", versionDescription)

  return document

}
