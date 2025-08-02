package com.ex_dock.ex_dock.database.text_pages

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class TextPagesJdbcVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus

  private val textPagesDeliveryOptions = DeliveryOptions().setCodecName("TextPagesCodec")

  companion object {
    private const val CACHE_ADDRESS = "text_pages"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize the eventbus connections with the text pages table
    getAllTextPages()
    getTextPageById()
    createTextPage()
    updateTextPage()
    deleteTextPage()

    return super.start()
  }

  private fun getAllTextPages() {
    val getAllTextPagesConsumer = eventBus.consumer<String>("process.text_pages.getAllTextPages")
    getAllTextPagesConsumer.handler { message ->
      val query = JsonObject()

      client.find("text_pages", query).replyListMessage(message)
    }
  }

  private fun getTextPageById() {
    val getTextPageByIdConsumer = eventBus.consumer<String>("process.text_pages.getTextPageById")
    getTextPageByIdConsumer.handler { message ->
      val id = message.body()
      val query = JsonObject()
        .put("_id", id)
      client.find("text_pages", query).replySingleMessage(message)
    }
  }

  private fun createTextPage() {
    val createTextPageConsumer = eventBus.consumer<TextPages>("process.text_pages.createTextPage")
    createTextPageConsumer.handler { message ->
      val textPages = message.body()
      val document = textPages.toDocument()

      val rowsFuture = client.save("text_pages", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String? = res
        if (lastInsertID != null) {
          textPages.textPagesId = lastInsertID
        }

        message.reply(textPages, textPagesDeliveryOptions)
      }
    }
  }

  private fun updateTextPage() {
    val updateTextPageConsumer = eventBus.consumer<TextPages>("process.text_pages.updateTextPage")
    updateTextPageConsumer.handler { message ->
      val textPages = message.body()
      val document = textPages.toDocument()

      if (textPages.textPagesId == null) {
        message.fail(400, "Text page id is null")
        return@handler
      }

      val rowsFuture = client.save("text_pages", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        message.reply(textPages, textPagesDeliveryOptions)
      }
    }
  }

  private fun deleteTextPage() {
    val deleteTextPageConsumer = eventBus.consumer<String>("process.text_pages.deleteTextPage")
    deleteTextPageConsumer.handler { message ->
      val id = message.body()
      val query = JsonObject()
        .put("text_pages_id", id)

      val rowsFuture = client.removeDocuments("text_pages", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        message.reply("Successfully deleted text page")
      }
    }
  }
}
