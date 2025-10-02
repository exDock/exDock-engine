package com.ex_dock.ex_dock.database.template

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class TemplateJdbcVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus

  private val failedMessage: String = "failed"
  private val templateDeliveryOptions = DeliveryOptions().setCodecName("TemplateCodec")

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    getAllTemplates()
    getTemplateByKey()
    createTemplate()
    updateTemplate()
    deleteTemplate()

    return super.start()
  }

  private fun getAllTemplates() {
    val getAllTemplatesConsumer = eventBus.consumer<String>("process.template.getAllTemplates")
    getAllTemplatesConsumer.handler { message ->
      val query = JsonObject()

      client.find("templates", query).replyListMessage(message)
    }
  }

  private fun getTemplateByKey() {
    val getTemplateByKeyConsumer = eventBus.consumer<String>("process.template.getTemplateByKey")
    getTemplateByKeyConsumer.handler { message ->
      val key = message.body()
      val query = JsonObject()
        .put("_id", key)

      client.find("templates", query).replySingleMessage(message)
    }
  }

  private fun createTemplate() {
    val createTemplateConsumer = eventBus.consumer<Template>("process.template.createTemplate")
    createTemplateConsumer.handler { message ->
      val template = message.body()
      val document = template.toDocument()

      val rowsFuture = client.save("templates", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        template.templateKey

        message.reply(template, templateDeliveryOptions)
      }
    }
  }

  private fun updateTemplate() {
    val updateTemplateConsumer = eventBus.consumer<Template>("process.template.updateTemplate")
    updateTemplateConsumer.handler { message ->
      val template = message.body()
      val document = template.toDocument()

      val rowsFuture = client.save("templates", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        template.templateKey

        message.reply(template, templateDeliveryOptions)
      }
    }
  }

  private fun deleteTemplate() {
    val deleteTemplateConsumer = eventBus.consumer<String>("process.template.deleteTemplate")
    deleteTemplateConsumer.handler { message ->
      val key = message.body()
      val query = JsonObject()
        .put("_id", key)

      val rowsFuture = client.removeDocuments("templates", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, "Failed to execute query: $res")
      }

      rowsFuture.onSuccess { res ->
        message.reply("Successfully deleted template")
      }
    }
  }
}
