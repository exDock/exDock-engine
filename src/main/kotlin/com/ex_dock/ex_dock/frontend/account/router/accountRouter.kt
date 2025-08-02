package com.ex_dock.ex_dock.frontend.account.router

import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.Permission
import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.web.Router
import io.vertx.ext.web.Session

fun Router.initAccount(vertx: Vertx) {
  val accountRouter = Router.router(vertx)
  val eventBus: EventBus = vertx.eventBus()
  val authHandler = ExDockAuthHandler(vertx)

  accountRouter.post("/create").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val permissions = body.getJsonArray("permissions")
    val permissionList = permissions.map { permission -> permission as JsonObject }
      .map { permission ->
        Pair(permission.getString("first"), Permission.fromString(permission.getString("second")))
      }
    val fullUser = FullUser(
      userId = null,
      email = body.getString("email"),
      password = body.getString("password"),
      permissions = permissionList,
      apiKey = null
    )

    eventBus.request<JsonObject>("process.account.createUser", fullUser, DeliveryOptions().setCodecName("FullUserCodec"))
      .onFailure { error ->
        ctx.end("Error creating user: ${error.localizedMessage}")
      }
      .onSuccess { res ->
        ctx.end("User created successfully: ${res.body()} ")
      }
  }

  accountRouter["/getAll"].handler { ctx ->
    eventBus.request<List<FullUser>>("process.account.getAllUsers", "")
      .onSuccess { reply ->
        ctx.end(reply.body().toString())
      }
      .onFailure { error ->
        ctx.end("Error retrieving account data: ${error.localizedMessage}")
      }
  }

  accountRouter.get("/").handler { ctx ->
    eventBus.request<Any>("process.account.getData", "testUser")
      .onSuccess { reply ->
        ctx.end(reply.body().toString())
      }
      .onFailure { error ->
        ctx.end("Error retrieving account data: ${error.localizedMessage}")
      }
  }

  //Test for login in with set credentials
  accountRouter["/test"].handler { ctx ->
    val session: Session = ctx.session()
    val credentials = UsernamePasswordCredentials("test@test.com", "123456")
    var user: User
    authHandler.authenticate(credentials).onComplete { authUser ->
      user = authUser.result()
      session.put("user", user)
      ctx.end("User Registered")
    }
  }

  //Test for login in with user from session
  accountRouter["/test2"].handler { ctx ->
    val session: Session = ctx.session()
    val user = session.get<User>("user")

    if (user != null) {
      ctx.end("User is authenticated: ${user.principal()}")
    } else {
      ctx.end("User is not authenticated")
    }
  }

  //Test if the user has permission to view the page
  accountRouter["/test3"].handler { ctx ->
    val session: Session = ctx.session()
    val user = session.get<User>("user")

    authHandler.verifyPermissionAuthorization(user, "userRead") {
      if (it.getBoolean("success")) {
        ctx.end("User has permission!")
      } else {
        ctx.end("User does not have permission: ${it.getString("message")}")
      }
    }
  }

  //Test if the user has permission to view the page
  accountRouter["/test4"].handler { ctx ->
    val session: Session = ctx.session()
    val user = session.get<User>("user")

    authHandler.verifyPermissionAuthorization(user, "serverRead") {
      if (it.getBoolean("success")) {
        ctx.end("User has permission!")
      } else {
        ctx.end(it.getString("message"))
      }
    }
  }

  this.route("/account*").subRouter(accountRouter)
}
