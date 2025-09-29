package com.ex_dock.ex_dock.database.auth

import com.ex_dock.ex_dock.database.account.FullUser
import com.ex_dock.ex_dock.database.account.Permission
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class AuthenticationVerticleTest {
  private lateinit var eventBus: EventBus
  private val userDeliveryOptions = DeliveryOptions().setCodecName("FullUserCodec")
  private val usernameDeliveryOptions = DeliveryOptions().setCodecName("UsernamePasswordCredentialsCodec")
  private val testUser = FullUser(
    userId = "1234567",
    email = "testEmail@test.com",
    password = "testPassword",
    permissions = listOf(Pair("testPermission", Permission.READ_WRITE)),
    apiKey = "testApiKey"
  )

  @BeforeEach
  @DisplayName("Deploy the verticle")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(FullUser::class)
    eventBus.registerGenericCodec(UsernamePasswordCredentials::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      AuthenticationVerticle::class.qualifiedName.toString(),
      AuthenticationVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      vertxTestContext.completeNow()
    }
  }

  @Test
  @DisplayName("Test handling login")
  fun testHandleLogin(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val credentials = UsernamePasswordCredentials(testUser.email, testUser.password)
    eventBus.request<String>("process.authentication.login", credentials, usernameDeliveryOptions).onFailure {
      vertxTestContext.completeNow()
    }.onSuccess { message ->
      vertxTestContext.failNow(Error("Login should have failed"))
    }
  }
}
