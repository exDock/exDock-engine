package com.ex_dock.ex_dock.database.category

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

class CategoryJdbcVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val categoriesDeliveryOptions = DeliveryOptions().setCodecName("CategoryInfoCodec")

  companion object {
    private const val CACHE_ADDRESS = "categories"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for basic categories
    getAllCategories()
    getCategoryById()
    createCategory()
    editCategory()
    deleteCategory()

    return Future.succeededFuture<Unit>()
  }


  private fun getAllCategories() {
    val getAllCategoriesConsumer = eventBus.consumer<String>("process.category.getAllCategories")
    getAllCategoriesConsumer.handler { message ->
      val query = JsonObject()
      client.find("categories", query).replyListMessage(message)
    }
  }

  private fun getCategoryById() {
    val getCategoryByIdConsumer = eventBus.consumer<String>("process.category.getCategoryById")
    getCategoryByIdConsumer.handler { message ->
      val categoryId = message.body()
      val query = JsonObject()
        .put("_id", categoryId)
      client.find("categories", query).replySingleMessage(message)
    }
  }

  private fun createCategory() {
    val createCategoryConsumer = eventBus.consumer<CategoryInfo>("process.category.createCategory")
    createCategoryConsumer.handler { message ->
      val category = message.body()
      val document = category.toDocument()

      val rowsFuture = client.save("categories", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        category.categoryId = lastInsertID

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(category, categoriesDeliveryOptions)
      }
    }
  }

  private fun editCategory() {
    val editCategoryConsumer = eventBus.consumer<CategoryInfo>("process.category.editCategory")
    editCategoryConsumer.handler { message ->
      val body = message.body()

      if (body.categoryId == null) {
        message.fail(400, "No category id provided")
        return@handler
      }
      val document = body.toDocument()
      val rowsFuture = client.save("categories", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        body.categoryId = lastInsertID

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, categoriesDeliveryOptions)
      }
    }
  }

  private fun deleteCategory() {
    val deleteCategoryConsumer = eventBus.consumer<String>("process.category.deleteCategory")
    deleteCategoryConsumer.handler { message ->
      val categoryId = message.body()
      val query = JsonObject()
        .put("_id", categoryId)
      val rowsFuture = client.removeDocument("categories", query)

      rowsFuture.onFailure { error ->
        println("Failed to execute query: $error")
        message.fail(400, "Failed to execute query: $error")
      }

      rowsFuture.onSuccess { res ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("Category deleted successfully")
      }
    }
  }
}
