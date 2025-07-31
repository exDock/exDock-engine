package com.ex_dock.ex_dock.database.account

import org.mindrot.jbcrypt.BCrypt

import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization

data class FullUser(
  var userId: String?,
  var email: String,
  var password: String,
  var permissions: List<Pair<String, Permission>>,
  var apiKey: String?
) {
  companion object {
    fun fromJson(json: JsonObject): FullUser {
      val userId = json.getString("userId")
      val email = json.getString("email")
      val password = json.getString("password")
      val permissions = json.getJsonArray("permissions")
      val apiKey = json.getString("apiKey")

      val permissionList = permissions.map { permission -> permission as JsonObject }
        .map { permission ->
          Pair(
            permission.getString("name"),
            Permission.fromString(permission.getString("type"))
          )
        }

      return FullUser(userId, email, password, permissionList, apiKey)
    }
  }
}

fun FullUser.convertUser(authHandler: ExDockAuthHandler): io.vertx.ext.auth.User {
  val authorizations = JsonArray()
  val principal = JsonObject()
    .put("id", this.userId)
    .put("email", this.email)
    .put("password", this.password)
    .put("authorizations", authorizations)
  var user = io.vertx.ext.auth.User.create(principal)
  val permissions = this.permissions

  for (permission in permissions) {
    user = user.addPermission(permission.second, permission.first, authHandler)
  }

  return user
}

fun FullUser.toDocument(): JsonObject {
  val permissionArray = JsonArray()
  this.permissions.forEach { (permissionName, permissionType) ->
    permissionArray.add(
      JsonObject()
        .put("name", permissionName)
        .put("type", permissionType.name)
    )
  }

  val document = JsonObject()
    .put("email", this.email)
    .put("password", this.password.hash())
    .put("permissions", permissionArray)
    .put("api_key", this.apiKey.orEmpty())

  if (this.userId != null) {
    document.put("_id", this.userId)
  }

  return document
}

fun io.vertx.ext.auth.User.addPermission(permission: Permission, task: String, authHandler: ExDockAuthHandler): io.vertx.ext.auth.User {
  return when (permission) {
    Permission.NONE -> this
    Permission.READ -> this.addAuth(task + Permission.READ.name, authHandler)
    Permission.WRITE -> this.addAuth(task + Permission.WRITE.name, authHandler)
    Permission.READ_WRITE -> {
      this.addAuth(task + Permission.READ_WRITE.name, authHandler)
      this.addAuth(task + Permission.WRITE.name, authHandler)
    }
  }
}

fun io.vertx.ext.auth.User.addAuth(name: String, authHandler: ExDockAuthHandler): io.vertx.ext.auth.User {
  val authorizations = this.principal().getJsonArray("authorizations")
  if (authHandler.saveAuthorization.contains(PermissionBasedAuthorization.create(name))) {
    this.authorizations().put(
      name,
      PermissionBasedAuthorization.create(name)
    )
    authorizations.add(name)
    this.principal().remove("authorizations")
    this.principal().put("authorizations", authorizations)
  } else {
    authHandler.saveAuthorization.add(PermissionBasedAuthorization.create(name))
    this.authorizations().put(
      name,
      PermissionBasedAuthorization.create(name)
    )
    authorizations.add(name)
    this.principal().remove("authorizations")
    this.principal().put("authorizations", authorizations)
  }

  return this
}

enum class Permission(name: String) {
  NONE("None"),
  READ("Read"),
  WRITE("Write"),
  READ_WRITE("Read-Write");

  companion object {
    fun fromString(value: String): Permission {
      return entries.find { it.name == value.lowercase() } ?: NONE
    }

    fun toString(permission: Permission): String {
      return permission.name.lowercase().replace("_", "-")
    }
  }
}

fun String.hash(): String {
  return BCrypt.hashpw(this, BCrypt.gensalt(12))
}

fun String.convertToPermission(): Permission {
  when (this) {
    "read" -> return Permission.READ
    "write" -> return Permission.WRITE
    "read-write" -> return Permission.READ_WRITE
  }

  return Permission.NONE
}

fun Permission.convertToString(): String {
  return when (this) {
    Permission.READ -> "read"
    Permission.WRITE -> "write"
    Permission.READ_WRITE -> "read-write"
    Permission.NONE -> "none"
  }
}
