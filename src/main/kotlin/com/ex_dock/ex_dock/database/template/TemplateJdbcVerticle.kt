package com.ex_dock.ex_dock.database.template

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class TemplateJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus

  private val failedMessage: String = "failed"
  private val templateDeliveryOptions = DeliveryOptions().setCodecName("TemplateCodec")
  private val blockDeliveryOptions = DeliveryOptions().setCodecName("BlockCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    getAllTemplates()
    getTemplateByKey()
    createTemplate()
    updateTemplate()
    deleteTemplate()

    getAllBlocks()
    getBlockByKey()
    createBlock()
    updateBlock()
    deleteBlock()
  }

  private fun getAllTemplates() {
    val allTemplateConsumer = eventBus.consumer<Any?>("process.templates.getAllTemplates")
    allTemplateConsumer.handler { message ->
      val query = "SELECT * FROM templates"
      val rowsFuture = client.preparedQuery(query).execute()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { rows ->
        val templates: MutableList<Template> = rows.map { makeTemplate(it) }.toMutableList()
        message.reply(templates, listDeliveryOptions)
      }
    }
  }

  private fun getTemplateByKey() {
    val getTemplateByKeyConsumer = eventBus.consumer<String>("process.templates.getTemplateByKey")
    getTemplateByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM templates WHERE template_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { rows ->
        if (rows.size() == 1) {
          val template = makeTemplate(rows.first())
          message.reply(template, templateDeliveryOptions)
        } else {
          message.reply(failedMessage)
        }
      }
    }
  }

  private fun createTemplate() {
    val createTemplateConsumer = eventBus.consumer<Template>("process.templates.createTemplate")
    createTemplateConsumer.handler { message ->
      val body = message.body()
      val isPutRequest = message.headers().contains("isPutRequest")
      val templateTuple = makeTemplateTuple(body, isPutRequest)
      val query = "INSERT INTO templates (template_data, template_key) VALUES (?,?)"

      val rowsFuture = client.preparedQuery(query).execute(templateTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply(body, templateDeliveryOptions)
      }
    }
  }

  private fun updateTemplate() {
    val updateTemplateConsumer = eventBus.consumer<Template>("process.templates.updateTemplate")
    updateTemplateConsumer.handler { message ->
      val body = message.body()
      val isPutRequest = message.headers().contains("isPutRequest")
      val templateTuple = makeTemplateTuple(body, isPutRequest)
      val query = "UPDATE templates SET template_data =? WHERE template_key =?"

      val rowsFuture = client.preparedQuery(query).execute(templateTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply(body, templateDeliveryOptions)
      }
    }
  }

  private fun deleteTemplate() {
    val deleteTemplateConsumer = eventBus.consumer<String>("process.templates.deleteTemplate")
    deleteTemplateConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM templates WHERE template_key =?"

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply("Successfully deleted template")
      }
    }
  }

  private fun getAllBlocks() {
    val allBlocksConsumer = eventBus.consumer<Any?>("process.blocks.getAllBlocks")
    allBlocksConsumer.handler { message ->
      val query = "SELECT * FROM blocks"
      val rowsFuture = client.preparedQuery(query).execute()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { rows ->
        val blocks: MutableList<Block> = rows.map { makeBlock(it) }.toMutableList()
        message.reply(blocks, listDeliveryOptions)
      }
    }
  }

  private fun getBlockByKey() {
    val getBlockByKeyConsumer = eventBus.consumer<String>("process.blocks.getBlockByKey")
    getBlockByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM blocks WHERE template_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { rows ->
        if (rows.size() == 1) {
          val block = makeBlock(rows.first())
          message.reply(block, blockDeliveryOptions)
        } else {
          message.reply(failedMessage)
        }
      }
    }
  }

  private fun createBlock() {
    val createBlockConsumer = eventBus.consumer<Block>("process.blocks.createBlock")
    createBlockConsumer.handler { message ->
      val body = message.body()
      val isPutRequest = message.headers().contains("isPutRequest")
      val blockTuple = makeBlockTuple(body, isPutRequest)
      val query = "INSERT INTO blocks (template_key) VALUES (?)"

      val rowsFuture = client.preparedQuery(query).execute(blockTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply(body, blockDeliveryOptions)
      }
    }
  }

  private fun updateBlock() {
    val updateBlockConsumer = eventBus.consumer<Block>("process.blocks.updateBlock")
    updateBlockConsumer.handler { message ->
      val body = message.body()
      val isPutRequest = message.headers().contains("isPutRequest")
      val blockTuple = makeBlockTuple(body, isPutRequest)
      val query = "UPDATE blocks SET template_key =? WHERE template_key =?"

      val rowsFuture = client.preparedQuery(query).execute(blockTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply(body, blockDeliveryOptions)
      }
    }
  }

  private fun deleteBlock() {
    val deleteBlockConsumer = eventBus.consumer<String>("process.blocks.deleteBlock")
    deleteBlockConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM blocks WHERE template_key =?"

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onSuccess { _ ->
        message.reply("Successfully deleted block")
      }
    }
  }

  private fun makeTemplate(row: Row): Template {
    return Template(
      row.getString("template_key"),
      row.getString("template_data"),
      row.getString("data_string")
    )
  }

  private fun makeBlock(row: Row): Block {
    return Block(
      row.getString("block_key"),
    )
  }

  private fun makeTemplateTuple(body: Template, isPutRequest: Boolean): Tuple {
    val templateTuple = if (isPutRequest) {
      Tuple.of(
        body.templateData,
        body.templateKey,
      )
    } else {
      Tuple.of(
        body.templateKey,
        body.templateData
      )
    }

    return templateTuple
  }

  private fun makeBlockTuple(body: Block, isPutRequest: Boolean): Tuple {
    val blockTuple = if (isPutRequest) {
      Tuple.of(
        body.templateKey,
        body.templateKey
      )
    } else {
      Tuple.of(
        body.templateKey
      )
    }

    return blockTuple
  }
}
