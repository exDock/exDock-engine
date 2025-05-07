package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductMultiSelectJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val multiSelectBoolDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectBoolCodec")
  private val multiSelectFloatDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectFloatCodec")
  private val multiSelectIntDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectIntCodec")
  private val multiSelectMoneyDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectMoneyCodec")
  private val multiSelectStringDeliveryOptions = DeliveryOptions().setCodecName("MultiSelectStringCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    getAllMultiSelectAttributesBool()
    getMultiSelectAttributesBoolByKey()
    createMultiSelectAttributesBool()
    updateMultiSelectAttributesBool()
    deleteMultiSelectAttributesBool()

    getAllMultiSelectAttributesFloat()
    getMultiSelectAttributesFloatByKey()
    createMultiSelectAttributesFloat()
    updateMultiSelectAttributesFloat()
    deleteMultiSelectAttributesFloat()

    getAllMultiSelectAttributesString()
    getMultiSelectAttributesStringByKey()
    createMultiSelectAttributesString()
    updateMultiSelectAttributesString()
    deleteMultiSelectAttributesString()

    getAllMultiSelectAttributesInt()
    getMultiSelectAttributesIntByKey()
    createMultiSelectAttributesInt()
    updateMultiSelectAttributesInt()
    deleteMultiSelectAttributesInt()

    getAllMultiSelectAttributesMoney()
    getMultiSelectAttributesMoneyByKey()
    createMultiSelectAttributesMoney()
    updateMultiSelectAttributesMoney()
    deleteMultiSelectAttributesMoney()

    getALlMultiSelectAttributesInfo()
    getMultiSelectAttributesInfoByKey()
  }

  private fun getAllMultiSelectAttributesBool() {
    val getAllMultiSelectAttributesBoolConsumer = eventBus.consumer<String>("process.multiSelect.getAllMultiSelectAttributesBool")
    getAllMultiSelectAttributesBoolConsumer.handler { message ->
      val query = "SELECT * FROM multi_select_attributes_bool"
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectBoolList: MutableList<MultiSelectBool> = emptyList<MultiSelectBool>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach{ row ->
            multiSelectBoolList.add(makeMultiSelectAttributesBool(row))
          }
        }

        message.reply(multiSelectBoolList, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesBoolByKey() {
    val getMultiSelectAttributesBoolByKeyConsumer = eventBus.consumer<MultiSelectBool>("process.multiSelect.getMultiSelectAttributesBoolByKey")
    getMultiSelectAttributesBoolByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM multi_select_attributes_bool WHERE attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeMultiSelectAttributesBool(rows.first()), multiSelectBoolDeliveryOptions)
        } else {
          message.reply("No website bool found")
        }
      }
    }
  }

  private fun createMultiSelectAttributesBool() {
    val createMultiSelectAttributesBoolConsumer = eventBus.consumer<MultiSelectBool>("process.multiSelect.createMultiSelectAttributesBool")
    createMultiSelectAttributesBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO multi_select_attributes_bool (attribute_key, option, value) VALUES (?, ?, ?::bit(1))"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesBoolTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectBoolDeliveryOptions)
      }
    }
  }

  private fun updateMultiSelectAttributesBool() {
    val updateMultiSelectAttributesBoolConsumer = eventBus.consumer<MultiSelectBool>("process.multiSelect.updateMultiSelectAttributesBool")
    updateMultiSelectAttributesBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE multi_select_attributes_bool SET option =?, value =?::bit(1) WHERE attribute_key =? "
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesBoolTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectBoolDeliveryOptions)
      }
    }
  }

  private fun deleteMultiSelectAttributesBool() {
    val deleteMultiSelectAttributesBoolConsumer = eventBus.consumer<MultiSelectBool>("process.multiSelect.deleteMultiSelectAttributesBool")
    deleteMultiSelectAttributesBoolConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM multi_select_attributes_bool WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("Multi-select attribute bool deleted successfully")
      }
    }
  }

  private fun getAllMultiSelectAttributesFloat() {
    val allMultiSelectAttributesFloatConsumer = eventBus.consumer<JsonObject>("process.multiSelect.getAllMultiSelectAttributesFloat")
    allMultiSelectAttributesFloatConsumer.handler { message ->
      val query = "SELECT * FROM multi_select_attributes_float"
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectFloatList: MutableList<MultiSelectFloat> = emptyList<MultiSelectFloat>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectFloatList.add(makeMultiSelectAttributesFloat(row))
          }
        }

        message.reply(multiSelectFloatList, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesFloatByKey() {
    val getMultiSelectAttributesFloatByKeyConsumer = eventBus.consumer<MultiSelectFloat>("process.multiSelect.getMultiSelectAttributesFloatByKey")
    getMultiSelectAttributesFloatByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM multi_select_attributes_float WHERE attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, multiSelectFloatDeliveryOptions)
        } else {
          message.reply("No website float found")
        }
      }
    }
  }

  private fun createMultiSelectAttributesFloat() {
    val createMultiSelectAttributesFloatConsumer = eventBus.consumer<MultiSelectFloat>("process.multiSelect.createMultiSelectAttributesFloat")
    createMultiSelectAttributesFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO multi_select_attributes_float (attribute_key, option, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesFloatTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectFloatDeliveryOptions)
      }
    }
  }

  private fun updateMultiSelectAttributesFloat() {
    val updateMultiSelectAttributesFloatConsumer = eventBus.consumer<MultiSelectFloat>("process.multiSelect.updateMultiSelectAttributesFloat")
    updateMultiSelectAttributesFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE multi_select_attributes_float SET option =?, value =? WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesFloatTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectFloatDeliveryOptions)
      }
    }
  }

  private fun deleteMultiSelectAttributesFloat() {
    val deleteMultiSelectAttributesFloatConsumer = eventBus.consumer<MultiSelectFloat>("process.multiSelect.deleteMultiSelectAttributesFloat")
    deleteMultiSelectAttributesFloatConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM multi_select_attributes_float WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("Multi-select attribute float deleted successfully")
      }
    }
  }

  private fun getAllMultiSelectAttributesString() {
    val allMultiSelectAttributesStringConsumer = eventBus.consumer<JsonObject>("process.multiSelect.getAllMultiSelectAttributesString")
    allMultiSelectAttributesStringConsumer.handler { message ->
      val query = "SELECT * FROM multi_select_attributes_string"
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectStringList: MutableList<MultiSelectString> = emptyList<MultiSelectString>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectStringList.add(makeMultiSelectAttributesString(row))
          }
        }

        message.reply(multiSelectStringList, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesStringByKey() {
    val getMultiSelectAttributesStringByKeyConsumer = eventBus.consumer<MultiSelectString>("process.multiSelect.getMultiSelectAttributesStringByKey")
    getMultiSelectAttributesStringByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM multi_select_attributes_string WHERE attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, multiSelectStringDeliveryOptions)
        } else {
          message.reply("No website string found")
        }
      }
    }
  }

  private fun createMultiSelectAttributesString() {
    val createMultiSelectAttributesStringConsumer = eventBus.consumer<MultiSelectString>("process.multiSelect.createMultiSelectAttributesString")
    createMultiSelectAttributesStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO multi_select_attributes_string (attribute_key, option, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesStringTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectStringDeliveryOptions)
      }
    }
  }

  private fun updateMultiSelectAttributesString() {
    val updateMultiSelectAttributesStringConsumer = eventBus.consumer<MultiSelectString>("process.multiSelect.updateMultiSelectAttributesString")
    updateMultiSelectAttributesStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE multi_select_attributes_string SET option =?, value =? WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesStringTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body,  multiSelectStringDeliveryOptions)
      }
    }
  }

  private fun deleteMultiSelectAttributesString() {
    val deleteMultiSelectAttributesStringConsumer = eventBus.consumer<MultiSelectString>("process.multiSelect.deleteMultiSelectAttributesString")
    deleteMultiSelectAttributesStringConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM multi_select_attributes_string WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("Multi-select attribute string deleted successfully")
      }
    }
  }

  private fun getAllMultiSelectAttributesInt() {
    val allMultiSelectAttributesIntConsumer = eventBus.consumer<String>("process.multiSelect.getAllMultiSelectAttributesInt")
    allMultiSelectAttributesIntConsumer.handler { message ->
      val query = "SELECT * FROM multi_select_attributes_int"
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectIntList: MutableList<MultiSelectInt> = emptyList<MultiSelectInt>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectIntList.add(makeMultiSelectAttributesInt(row))
          }
        }

        message.reply(multiSelectIntList, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesIntByKey() {
    val getMultiSelectAttributesIntByKeyConsumer = eventBus.consumer<MultiSelectInt>("process.multiSelect.getMultiSelectAttributesIntByKey")
    getMultiSelectAttributesIntByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM multi_select_attributes_int WHERE attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, multiSelectIntDeliveryOptions)
        } else {
          message.reply("No rows returned")
        }
      }
    }
  }

  private fun createMultiSelectAttributesInt() {
    val createMultiSelectAttributesIntConsumer = eventBus.consumer<MultiSelectInt>("process.multiSelect.createMultiSelectAttributesInt")
    createMultiSelectAttributesIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO multi_select_attributes_int (attribute_key, option, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesIntTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectIntDeliveryOptions)
      }
    }
  }

  private fun updateMultiSelectAttributesInt() {
    val updateMultiSelectAttributesIntConsumer = eventBus.consumer<MultiSelectInt>("process.multiSelect.updateMultiSelectAttributesInt")
    updateMultiSelectAttributesIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE multi_select_attributes_int SET option =?, value =? WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesIntTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectIntDeliveryOptions)
      }
    }
  }

  private fun deleteMultiSelectAttributesInt() {
    val deleteMultiSelectAttributesIntConsumer = eventBus.consumer<MultiSelectInt>("process.multiSelect.deleteMultiSelectAttributesInt")
    deleteMultiSelectAttributesIntConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM multi_select_attributes_int WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("Multi-select attribute int deleted successfully")
      }
    }
  }

  private fun getAllMultiSelectAttributesMoney() {
    val allMultiSelectAttributesMoneyConsumer = eventBus.consumer<String>("process.multiSelect.getAllMultiSelectAttributesMoney")
    allMultiSelectAttributesMoneyConsumer.handler { message ->
      val query = "SELECT * FROM multi_select_attributes_money"
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectMoney: MutableList<MultiSelectMoney> = emptyList<MultiSelectMoney>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectMoney.add(makeMultiSelectAttributesMoney(row))
          }
        }

        message.reply(multiSelectMoney, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesMoneyByKey() {
    val getMultiSelectAttributesMoneyByKeyConsumer = eventBus.consumer<MultiSelectMoney>("process.multiSelect.getMultiSelectAttributesMoneyByKey")
    getMultiSelectAttributesMoneyByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM multi_select_attributes_money WHERE attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, multiSelectMoneyDeliveryOptions)
        } else {
          message.reply("No Multi-select attribute Money found")
        }
      }
    }
  }

  private fun createMultiSelectAttributesMoney() {
    val createMultiSelectAttributesMoneyConsumer = eventBus.consumer<MultiSelectMoney>("process.multiSelect.createMultiSelectAttributesMoney")
    createMultiSelectAttributesMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO multi_select_attributes_money (attribute_key, option, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesMoneyTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectMoneyDeliveryOptions)
      }
    }
  }

  private fun updateMultiSelectAttributesMoney() {
    val updateMultiSelectAttributesMoneyConsumer = eventBus.consumer<MultiSelectMoney>("process.multiSelect.updateMultiSelectAttributesMoney")
    updateMultiSelectAttributesMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE multi_select_attributes_money SET option =?, value =? WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeMultiSelectAttributesMoneyTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, multiSelectMoneyDeliveryOptions)
      }
    }
  }

  private fun deleteMultiSelectAttributesMoney() {
    val deleteMultiSelectAttributesMoneyConsumer = eventBus.consumer<MultiSelectMoney>("process.multiSelect.deleteMultiSelectAttributesMoney")
    deleteMultiSelectAttributesMoneyConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM multi_select_attributes_money WHERE attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("Multi-select attribute money deleted successfully")
      }
    }
  }

  private fun getALlMultiSelectAttributesInfo() {
    val allMultiSelectAttributesInfoConsumer = eventBus.consumer<String>("process.multiSelect.getAllMultiSelectAttributesInfo")
    allMultiSelectAttributesInfoConsumer.handler { message ->
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, products.short_description, msab.value AS bool_value, " +
        "msaf.value AS float_value, msas.value AS string_value, " +
        "msai.value AS int_value, msam.value AS money_value, " +
        "cpa.attribute_key FROM products " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_bool msab on msab.attribute_key = cpa.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_float msaf on cpa.attribute_key = msaf.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_int msai on cpa.attribute_key = msai.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_string msas on cpa.attribute_key = msas.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_money msam on cpa.attribute_key = msam.attribute_key "
      val rowsFuture = client.preparedQuery(query).execute()
      val multiSelectInfoList: MutableList<MultiSelectInfo> = emptyList<MultiSelectInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectInfoList.add(makeMultiSelectAttributesInfo(row))
          }
        }

        message.reply(multiSelectInfoList, listDeliveryOptions)
      }
    }
  }

  private fun getMultiSelectAttributesInfoByKey() {
    val getMultiSelectAttributesInfoByKeyConsumer = eventBus.consumer<Int>("process.multiSelect.getMultiSelectAttributesInfoByKey")
    getMultiSelectAttributesInfoByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, products.short_description, msab.value AS bool_value, " +
        "msaf.value AS float_value, msas.value AS string_value, " +
        "msai.value AS int_value, msam.value AS money_value, " +
        "cpa.attribute_key FROM products " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_bool msab on msab.attribute_key = cpa.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_float msaf on cpa.attribute_key = msaf.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_int msai on cpa.attribute_key = msai.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_string msas on cpa.attribute_key = msas.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_money msam on cpa.attribute_key = msam.attribute_key " +
        "WHERE products.product_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))
      val multiSelectInfoList: MutableList<MultiSelectInfo> = emptyList<MultiSelectInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            multiSelectInfoList.add(makeMultiSelectAttributesInfo(row))
          }
        }

        message.reply(multiSelectInfoList, listDeliveryOptions)
      }
    }
  }

  private fun makeMultiSelectAttributesBool(row: Row): MultiSelectBool {
    return MultiSelectBool(
      attributeKey = row.getString("attribute_key"),
      option = row.getInteger("option"),
      value = row.getBoolean("value")
    )
  }

  private fun makeMultiSelectAttributesFloat(row: Row): MultiSelectFloat {
    return MultiSelectFloat(
      attributeKey = row.getString("attribute_key"),
      option = row.getInteger("option"),
      value = row.getFloat("value")
    )
  }

  private fun makeMultiSelectAttributesString(row: Row): MultiSelectString {
    return MultiSelectString(
      attributeKey = row.getString("attribute_key"),
      option = row.getInteger("option"),
      value = row.getString("value")
    )
  }

  private fun makeMultiSelectAttributesInt(row: Row): MultiSelectInt {
    return MultiSelectInt(
      attributeKey = row.getString("attribute_key"),
      option = row.getInteger("option"),
      value = row.getInteger("value")
    )
  }

  private fun makeMultiSelectAttributesMoney(row: Row): MultiSelectMoney {
    return MultiSelectMoney(
      attributeKey = row.getString("attribute_key"),
      option = row.getInteger("option"),
      value = row.getDouble("value")
    )
  }

  private fun makeMultiSelectAttributesInfo(row: Row): MultiSelectInfo {
    return MultiSelectInfo(
      Products(
        productId = row.getInteger("product_id"),
        name = row.getString("name"),
        shortName = row.getString("short_name"),
        description = row.getString("description"),
        shortDescription = row.getString("short_description"),
      ),
      row.getString("attribute_key"),
      row.getBoolean("bool_value"),
      row.getFloat("float_value"),
      row.getString("string_value"),
      row.getInteger("int_value"),
      row.getDouble("money_value")
    )
  }

  private fun makeMultiSelectAttributesBoolTuple(body: MultiSelectBool, isPutRequest: Boolean): Tuple {
    val multiSelectBoolTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.option,
        body.value.toInt(),
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.attributeKey,
        body.option,
        body.value.toInt(),
      )
    }

    return multiSelectBoolTuple
  }

  private fun makeMultiSelectAttributesFloatTuple(body: MultiSelectFloat, isPutRequest: Boolean): Tuple {
    val multiSelectFloatTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.option,
        body.value,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.attributeKey,
        body.option,
        body.value,
      )
    }

    return multiSelectFloatTuple
  }

  private fun makeMultiSelectAttributesStringTuple(body: MultiSelectString, isPutRequest: Boolean): Tuple {
    val multiSelectStringTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.option,
        body.value,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.attributeKey,
        body.option,
        body.value,
      )
    }

    return multiSelectStringTuple
  }

  private fun makeMultiSelectAttributesIntTuple(body: MultiSelectInt, isPutRequest: Boolean): Tuple {
    val multiSelectIntTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.option,
        body.value,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.attributeKey,
        body.option,
        body.value,
      )
    }

    return multiSelectIntTuple
  }

  private fun makeMultiSelectAttributesMoneyTuple(body: MultiSelectMoney, isPutRequest: Boolean): Tuple {
    val multiSelectMoneyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.option,
        body.value,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.attributeKey,
        body.option,
        body.value,
      )
    }

    return multiSelectMoneyTuple
  }

  private fun Boolean.toInt() = if (this) 1 else 0
}
