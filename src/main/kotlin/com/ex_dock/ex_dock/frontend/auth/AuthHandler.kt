package com.ex_dock.ex_dock.frontend.auth

import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.convertUser
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User as VertxUser
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
   * Authenticates the user with given UsernamePasswordCredentials
   */
  override fun authenticate(credentials: Credentials?): Future<VertxUser> {
    // Test if credentials are not null
    if (credentials == null) {
      return Future.failedFuture<VertxUser>("Credentials cannot be null")
    }

    val jsonCredentials = credentials.toJson()
    val email = jsonCredentials.getString("username")
    val password = jsonCredentials.getString("password")

    return Future.future { future ->
      eventBus.request<FullUser>("process.account.getFullUserByEmail", email).onComplete {
        if (it.succeeded()) {
          val user = it.result().body()
          // Check if the password matches the hashed password in the database
          if (BCrypt.checkpw(password, user.user.password)) {
            future.complete(user.convertUser(this))
          } else {
            future.fail("Invalid password")
          }
        } else {
          future.fail("User not found")
        }
      }
    }
  }

  /**
   * Verify if the user is authorized to view the requested resource
   */
  fun verifyPermissionAuthorization(user: VertxUser, task: String, callBack: Consumer<JsonObject>) {
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
