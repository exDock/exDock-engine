package com.ex_dock.ex_dock.database.account

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class AccountJdbcVerticle: VerticleBase() {

  private lateinit var client: MongoClient

  private lateinit var eventBus: EventBus


  companion object {
    const val FAILED = "failed"
    const val USER_DELETED_SUCCESS = "User deleted successfully"
    const val CACHE_ADDRESS = "accounts"
  }

  private val fullUserDeliveryOptions = DeliveryOptions().setCodecName("FullUserCodec")

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for user management
    getAllUsers()
    getUserById()
    getUserByEmail()
    createUser()
    updateUser()
    deleteUser()

    return Future.succeededFuture<Unit>()
  }

  private fun getAllUsers() {
    val allUserDataConsumer = eventBus.consumer<String>("process.account.getAllUsers")
    allUserDataConsumer.handler { message ->
      val query = JsonObject()
      client.find("users", query).replyListMessage(message)
    }
  }

  private fun getUserById() {
    val getUserByIdConsumer = eventBus.consumer<String>("process.account.getUserById")
    getUserByIdConsumer.handler { message ->
      val userId = message.body()
      val query = JsonObject()
        .put("_id", userId)
      client.find("users", query).replySingleMessage(message)
    }
  }

  private fun getUserByEmail() {
    val getUserByEmailConsumer = eventBus.consumer<String>("process.account.getUserByEmail")
    getUserByEmailConsumer.handler { message ->
      val email = message.body()
      val query = JsonObject()
        .put("email", email)
      client.find("users", query).replySingleMessage(message)
    }
  }

  private fun createUser() {
    val createUserConsumer = eventBus.consumer<FullUser>("process.account.createUser")
    createUserConsumer.handler { message ->
      val user = message.body()
      val document = user.toDocument()

      val rowsFuture = client.save("users", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, FAILED)
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          user.userId = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(user, fullUserDeliveryOptions)
      }
    }
  }

  private fun updateUser() {
    val updateUserConsumer = eventBus.consumer<FullUser>("process.account.updateUser")
    updateUserConsumer.handler { message ->
      val body = message.body()

      if (body.userId == null) {
        message.fail(400, FAILED)
        return@handler
      }

      val document = body.toDocument()
      val rowsFuture = client.save("users", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, FAILED)
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          body.userId = lastInsertID
        }

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, fullUserDeliveryOptions)
      }
    }
  }

  private fun deleteUser() {
    val deleteUserConsumer = eventBus.consumer<String>("process.account.deleteUser")
    deleteUserConsumer.handler { message ->
      val userId = message.body()
      val query = JsonObject()
        .put("_id", userId)
      val rowsFuture = client.removeDocument("users", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, FAILED)
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(USER_DELETED_SUCCESS)
      }
    }
  }
}
