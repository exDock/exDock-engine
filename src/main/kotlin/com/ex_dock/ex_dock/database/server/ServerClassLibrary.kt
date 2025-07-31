package com.ex_dock.ex_dock.database.server

import io.vertx.core.json.JsonObject

data class ServerDataData(
  val key: String,
  var value: String,
  )

data class ServerVersionData(
  val major: Int,
  val minor: Int,
  val patch: Int,
  var versionName: String,
  var versionDescription: String,
  )


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
