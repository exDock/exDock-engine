package com.ex_dock.ex_dock.frontend.auth

import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.Permission
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.ext.auth.authorization.Authorization
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization
import org.mindrot.jbcrypt.BCrypt
import java.util.Base64
import java.util.function.Consumer

/**
 * Custom implementation of the authentication provider.
 * All authentication methods are in this class
 */
class ExDockAuthHandler(vertx: Vertx) : AuthenticationProvider {
  private val eventBus: EventBus = vertx.eventBus()
  private val authorizationsObject = JsonArray()
  val saveAuthorization: MutableSet<Authorization> = setOf(
    PermissionBasedAuthorization.create("userRead"),
    PermissionBasedAuthorization.create("userWrite"),
    PermissionBasedAuthorization.create("serverRead"),
    PermissionBasedAuthorization.create("serverWrite"),
    PermissionBasedAuthorization.create("templateRead"),
    PermissionBasedAuthorization.create("templateWrite"),
    PermissionBasedAuthorization.create("categoryContentRead"),
    PermissionBasedAuthorization.create("categoryContentWrite"),
    PermissionBasedAuthorization.create("categoryProductRead"),
    PermissionBasedAuthorization.create("categoryProductWrite"),
    PermissionBasedAuthorization.create("productContentRead"),
    PermissionBasedAuthorization.create("productContentWrite"),
    PermissionBasedAuthorization.create("productPriceRead"),
    PermissionBasedAuthorization.create("productPriceWrite"),
    PermissionBasedAuthorization.create("productWarehouseRead"),
    PermissionBasedAuthorization.create("productWarehouseWrite"),
    PermissionBasedAuthorization.create("textPagesRead"),
    PermissionBasedAuthorization.create("textPagesWrite")
  ).toMutableSet()

  /**
   * Authenticate the user based on the given credentials
   */
  @Deprecated("Deprecated in Kotlin")
  override fun authenticate(credentials: JsonObject?, resultHandler: Handler<AsyncResult<User>>?) {
    // this is required for inheriting
  }

  /**
   * Authenticates the user with given UsernamePasswordCredentials
   */
  override fun authenticate(credentials: Credentials?, resultHandler: Handler<AsyncResult<User>>?) {
    // Test if credentials are not null
    if (credentials == null) {
      resultHandler?.handle(Future.failedFuture("Missing credentials"))
      return
    }

    val jsonCredentials = credentials.toJson()
    val email = jsonCredentials.getString("username")
    val password = jsonCredentials.getString("password")

    // Request a user based on the given email
    eventBus.request<FullUser>("process.account.getFullUserByEmail", email).onComplete {
      if (it.succeeded()) {
        val user = it.result().body()
        // Check if the password matches the hashed password in the database
        if (BCrypt.checkpw(password, user.user.password)) {
          resultHandler?.handle(Future.succeededFuture(convertUser(user)))
        } else {
          resultHandler?.handle(Future.failedFuture("Invalid password"))
        }
      } else {
        resultHandler?.handle(Future.failedFuture("User not found"))
      }
    }
  }

  fun authenticateToken(token: String?, resultHandler: Handler<AsyncResult<User>>?) {

    if (token == null) {
      resultHandler?.handle(Future.failedFuture("Missing token"))
      return
    }

    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val authorizations = payload.split("[")[1].split("]")[0]

    if (authorizations.isNotEmpty()) {

    }
  }

  /**
   * Converts the full User from the database to a vertx user
   */
  private fun convertUser(fullUser: FullUser): User {
    // Clear previous conversion
    authorizationsObject.clear()

    val exDockUser = fullUser.user
    val principal = JsonObject()
      .put("id", exDockUser.userId)
      .put("email", exDockUser.email)
      .put("password", exDockUser.password)
    var user = User.create(principal)

    user = addPermission(fullUser.backendPermissions.userPermission, "user", user)
    user = addPermission(fullUser.backendPermissions.serverSettings, "server", user)
    user = addPermission(fullUser.backendPermissions.template, "template", user)
    user = addPermission(fullUser.backendPermissions.categoryContent, "categoryContent", user)
    user = addPermission(fullUser.backendPermissions.categoryProducts, "categoryProducts", user)
    user = addPermission(fullUser.backendPermissions.productContent, "productContent", user)
    user = addPermission(fullUser.backendPermissions.productPrice, "productPrice", user)
    user = addPermission(fullUser.backendPermissions.productWarehouse, "productWarehouse", user)
    user = addPermission(fullUser.backendPermissions.textPages, "textPages", user)

    user.principal().put("authorizations", authorizationsObject)

    return user
  }

  /**
   * Adds permissions to the user account based on full user permissions
   */
  private fun addPermission(permission: Permission, task: String, user: User): User {
    return when (permission) {
      Permission.NONE -> user
      Permission.READ -> addAuth(task + Permission.READ.name, user)
      Permission.WRITE -> addAuth(task + Permission.WRITE.name, user)
      Permission.READ_WRITE -> {
        val newUser = addAuth(task + Permission.READ.name, user)
        addAuth(task + Permission.WRITE.name, newUser)
      }
    }
  }

  /**
   * Adds unknown authorizations to the list of authorizations
   */
  private fun addAuth(name: String, user: User): User {
    if (saveAuthorization.contains(PermissionBasedAuthorization.create(name))) {
      user.authorizations().add(
        name,
        PermissionBasedAuthorization.create(name)
      )
      authorizationsObject.add(name)
    } else {
      saveAuthorization.add(PermissionBasedAuthorization.create(name))
      user.authorizations().add(
        name,
        PermissionBasedAuthorization.create(name)
      )
      authorizationsObject.add(name)
    }

    return user
  }

  /**
   * Verify if the user is authorized to view the requested resource
   */
  fun verifyPermissionAuthorization(user: User, task: String, callBack: Consumer<JsonObject>) {
    if (saveAuthorization.contains(PermissionBasedAuthorization.create(task))
      && saveAuthorization.first { authorization -> authorization == PermissionBasedAuthorization.create(task) }
        .match(user)
    ) {
      callBack.accept(JsonObject().apply {
        put("success", true)
      })
    } else {
      callBack.accept(JsonObject().apply {
        put("success", false)
        put("message", "Permission denied for task: $task")
      })
    }
  }

  fun verifyPermissionAuthorization(token: String, task: String, callBack: Consumer<JsonObject>) {
    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val authorizations = payload.split("[")[1].split("]")[0]
    if (authorizations.contains(task)) {
      callBack.accept(JsonObject().apply {
        put("success", true)
      })
    } else {
      callBack.accept(JsonObject().apply {
        put("success", false)
        put("message", "Permission denied for task: $task")
      })
    }
  }
}
