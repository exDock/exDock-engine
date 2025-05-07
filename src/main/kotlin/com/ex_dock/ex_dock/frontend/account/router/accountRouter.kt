package com.ex_dock.ex_dock.frontend.account.router

import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.web.Router
import io.vertx.ext.web.Session

fun Router.initAccount(vertx: Vertx) {
  val accountRouter = Router.router(vertx)
  val eventBus: EventBus = vertx.eventBus()
  val authHandler = ExDockAuthHandler(vertx)

  accountRouter.get("/").handler { ctx ->
    eventBus.request<Any>("process.account.getData", "testUser")
      .onSuccess{ reply ->
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
    authHandler.authenticate(credentials).onComplete {
      authUser -> user = authUser.result()
      session.put("user", user)
      ctx.end("User Registered")
    }
  }

  //Test for login in with user from session
  accountRouter["/test2"].handler { ctx ->
    val session: Session = ctx.session()
    val user = session.get<User>("user")

    if (user!= null) {
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
