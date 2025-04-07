package com.ex_dock.ex_dock.database.account

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
