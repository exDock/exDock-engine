package com.ex_dock.ex_dock.database.account

import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization

data class User(var userId: Int, var email: String, var password: String)

data class UserCreation(var email: String, var password: String)

data class BackendPermissions(
    val userId: Int,
    var userPermission: Permission,
    var serverSettings: Permission,
    var template: Permission,
    var categoryContent: Permission,
    var categoryProducts: Permission,
    var productContent: Permission,
    var productPrice: Permission,
    var productWarehouse: Permission,
    var textPages: Permission,
    var apiKey: String?
)

data class FullUser(var user: User, var backendPermissions: BackendPermissions) {
  init {
    require(user.userId == backendPermissions.userId)
  }
}

fun FullUser.convertUser(authHandler: ExDockAuthHandler): io.vertx.ext.auth.User {
  val authorizations = JsonArray()
  val exDockUser = this.user
  val principal = JsonObject()
    .put("id", exDockUser.userId)
    .put("email", exDockUser.email)
    .put("password", exDockUser.password)
    .put("authorizations", authorizations)
  var user = io.vertx.ext.auth.User.create(principal)

  user = user.addPermission(this.backendPermissions.userPermission, "user", authHandler)
  user = user.addPermission(this.backendPermissions.serverSettings, "server", authHandler)
  user = user.addPermission(this.backendPermissions.template, "template", authHandler)
  user = user.addPermission(this.backendPermissions.categoryContent, "categoryContent", authHandler)
  user = user.addPermission(this.backendPermissions.categoryProducts, "categoryProducts", authHandler)
  user = user.addPermission(this.backendPermissions.productContent, "productContent", authHandler)
  user = user.addPermission(this.backendPermissions.productPrice, "productPrice", authHandler)
  user = user.addPermission(this.backendPermissions.productWarehouse, "productWarehouse", authHandler)
  user = user.addPermission(this.backendPermissions.textPages, "textPages", authHandler)

  return user
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
    this.authorizations().add(
      name,
      PermissionBasedAuthorization.create(name)
    )
    authorizations.add(name)
    this.principal().remove("authorizations")
    this.principal().put("authorizations", authorizations)
  } else {
    authHandler.saveAuthorization.add(PermissionBasedAuthorization.create(name))
    this.authorizations().add(
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
      return values().find { it.name == value.lowercase() } ?: NONE
    }

    fun toString(permission: Permission): String {
      return permission.name.lowercase().replace("_", "-")
    }
  }
}
