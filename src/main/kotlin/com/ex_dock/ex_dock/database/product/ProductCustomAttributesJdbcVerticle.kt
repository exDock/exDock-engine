package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductCustomAttributesJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val customProductAttributesDataDeliveryOptions = DeliveryOptions().setCodecName("CustomProductAttributesCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    getAllCustomAttributes()
    getCustomAttributeByKey()
    createCustomAttribute()
    updateCustomAttribute()
    deleteCustomAttribute()
  }

  private fun getAllCustomAttributes() {
    val getAllCustomAttributesConsumer = eventBus.consumer<String>("process.attributes.getAllCustomAttributes")
    getAllCustomAttributesConsumer.handler { message ->
      val query = "SELECT * FROM custom_product_attributes"
      val rowsFuture = client.preparedQuery(query).execute()
      val customProductAttributesList: MutableList<CustomProductAttributes> = emptyList<CustomProductAttributes>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            customProductAttributesList.add(row.makeCustomAttribute())
          }
        }

        message.reply(customProductAttributesList, listDeliveryOptions)
      }
    }
  }

  private fun getCustomAttributeByKey() {
    val getCustomAttributeByKeyConsumer = eventBus.consumer<String>("process.attributes.getCustomAttributeByKey")
    getCustomAttributeByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM custom_product_attributes WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeCustomAttribute(), customProductAttributesDataDeliveryOptions)
        } else {
          message.reply("No custom attributes found")
        }
      }
    }
  }

  private fun createCustomAttribute() {
    val createCustomAttributeConsumer = eventBus.consumer<CustomProductAttributes>("process.attributes.createCustomAttribute")
    createCustomAttributeConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO custom_product_attributes (attribute_key, scope, name, type, multiselect, required) " +
          "VALUES (?,?,?,?::cpa_type,?::bit(1),?::bit(1))"

      val ctaTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(ctaTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          message.reply(body, customProductAttributesDataDeliveryOptions)
        } else {
          message.reply("Failed to create custom attribute")
        }
      }
    }
  }

  private fun updateCustomAttribute() {
    val updateCustomAttributeConsumer = eventBus.consumer<CustomProductAttributes>("process.attributes.updateCustomAttribute")
    updateCustomAttributeConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE custom_product_attributes SET scope=?, name=?, type=?::cpa_type, " +
          "multiselect=?::bit(1), required=?::bit(1) WHERE attribute_key=?"

      val ctaTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(ctaTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          message.reply(body, customProductAttributesDataDeliveryOptions)
        } else {
          message.reply("No custom attribute found to update")
        }
      }
    }
  }

  private fun deleteCustomAttribute() {
    val deleteCustomAttributeConsumer = eventBus.consumer<String>("process.attributes.deleteCustomAttribute")
    deleteCustomAttributeConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM custom_product_attributes WHERE attribute_key=?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          message.reply("Custom attribute deleted successfully")
        } else {
          message.reply("No custom attribute found to delete")
        }
      }
    }
  }

  private fun Row.makeCustomAttribute(): CustomProductAttributes {
    return CustomProductAttributes(
      attributeKey = this.getString("attribute_key"),
      scope = this.getInteger("scope"),
      name = this.getString("name"),
      type = this.getString("type").toType(),
      multiselect = this.getBoolean("multiselect"),
      required = this.getBoolean("required")
    )
  }

  private fun CustomProductAttributes.toTuple(isPutRequest: Boolean): Tuple {
    val ctaTuple = if (isPutRequest) {
      Tuple.of(
        this.scope,
        this.name,
        this.type.convertToString(),
        this.multiselect.toInt(),
        this.required.toInt(),
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.attributeKey,
        this.scope,
        this.name,
        this.type.convertToString(),
        this.multiselect.toInt(),
        this.required.toInt(),
      )
    }

    return ctaTuple
  }
}
