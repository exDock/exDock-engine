package com.ex_dock.ex_dock.database.account

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class AccountJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val fullUserDeliveryOptions = DeliveryOptions().setCodecName("FullUserCodec")
  private val testUser = FullUser(
    userId = "1234567",
    email = "testEmail@test.com",
    password = "testPassword",
    permissions = listOf(Pair("testPermission", Permission.READ_WRITE)),
    apiKey = "testApiKey"
  )

  @Test
  @DisplayName("Test the account classes functions")
  fun testAccountClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testAccountClassesFunctions")

    suite.test("testFullUserToJson") { context ->
      val expectedPermissions = JsonArray()
      testUser.permissions.forEach {
        expectedPermissions.add(JsonObject().put("first", it.first).put("second", Permission.toString(it.second)))
      }

      val expectedJson = JsonObject()
        .put("_id", testUser.userId)
        .put("email", testUser.email)
        .put("permissions", expectedPermissions)
        .put("api_key", testUser.apiKey)

      val result = testUser.toDocument()
      result.remove("password")

      context.assertEquals(result, expectedJson)
    }.test("testFullUserFromJson") { context ->
      val userJson = testUser.toDocument()
      val user = FullUser.fromJson(userJson)
      context.assertEquals(testUser.userId, user.userId)
      context.assertEquals(testUser.email, user.email)
      context.assertEquals(testUser.permissions, user.permissions)
      context.assertEquals(testUser.apiKey, user.apiKey)
    }

    suite.run(vertx).handler { res ->
      if (res.succeeded()) {
        context.completeNow()
      } else {
        context.failNow(res.cause())
      }
    }

  }

  @BeforeEach
  @DisplayName("Add the account to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(FullUser::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      AccountJdbcVerticle::class.qualifiedName.toString(),
      AccountJdbcVerticle
      ::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<FullUser>("process.account.createUser", testUser, fullUserDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val user = message.body()
        assertEquals(testUser.userId, user.userId)
        assertEquals(testUser.email, user.email)
        assertEquals(testUser.permissions, user.permissions)
        assertEquals(testUser.apiKey, user.apiKey)

        vertxTestContext.completeNow()
      }
    }
  }

  @Test
  @DisplayName("Test getting all users from the database")
  fun testGetAllUsers(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<List<JsonObject>>("process.account.getAllUsers", "").onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val users = message.body()
      assertTrue(users.isNotEmpty())
      vertxTestContext.completeNow()
    }
  }

  @Test
  @DisplayName("Test getting a user by id from the database")
  fun testGetUserById(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.account.getUserById", testUser.userId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val user = message.body()
      assertEquals(testUser.userId, user.getString("_id"))
      assertEquals(testUser.email, user.getString("email"))
      assertEquals(testUser.apiKey, user.getString("api_key"))
      vertxTestContext.completeNow()
    }
  }

  @Test
  @DisplayName("Test getting a user by email from the database")
  fun testGetUserByEmail(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.account.getUserByEmail", testUser.email).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val user = message.body()
      assertEquals(testUser.userId, user.getString("_id"))
      assertEquals(testUser.email, user.getString("email"))
      assertEquals(testUser.apiKey, user.getString("api_key"))
      vertxTestContext.completeNow()
    }
  }

  @Test
  @DisplayName("Test updating a user in the database")
  fun testUpdateUser(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedUser = FullUser(
      userId = testUser.userId,
      email = "updatedEmail@test.com",
      password = "updatedPassword",
      permissions = listOf(Pair("updatedPermission", Permission.READ_WRITE)),
      apiKey = "updated key",
    )
    eventBus.request<FullUser>("process.account.updateUser", updatedUser, fullUserDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val user = message.body()
      assertEquals(updatedUser.userId, user.userId)
      assertEquals(updatedUser.email, user.email)
      assertEquals(updatedUser.permissions, user.permissions)
      assertEquals(updatedUser.apiKey, user.apiKey)
      vertxTestContext.completeNow()
    }
  }

  @AfterEach
  @DisplayName("Remove the account from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.account.deleteUser", testUser.userId).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess {
      vertxTestContext.completeNow()
    }
  }
}
