package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductGlobalEavJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val eavGlobalBoolDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalBoolCodec")
  private val eavGlobalFloatDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalFloatCodec")
  private val eavGlobalIntDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalIntCodec")
  private val eavGlobalMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalMoneyCodec")
  private val eavGlobalMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalMultiSelectCodec")
  private val eavGlobalStringDeliveryOptions = DeliveryOptions().setCodecName("EavGlobalStringCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    getAllEavGlobalBool()
    getEavGlobalBoolByKey()
    createEavGlobalBool()
    updateEavGlobalBool()
    deleteEavGlobalBool()

    getAllEavGlobalFloat()
    getEavGlobalFloatByKey()
    createEavGlobalFloat()
    updateEavGlobalFloat()
    deleteEavGlobalFloat()

    getAllEavGlobalString()
    getEavGlobalStringByKey()
    createEavGlobalString()
    updateEavGlobalString()
    deleteEavGlobalString()

    getAllEavGlobalInt()
    getEavGlobalIntByKey()
    createEavGlobalInt()
    updateEavGlobalInt()
    deleteEavGlobalInt()

    getAllEavGlobalMoney()
    getEavGlobalMoneyByKey()
    createEavGlobalMoney()
    updateEavGlobalMoney()
    deleteEavGlobalMoney()

    getAllEavGlobalMultiSelect()
    getEavGlobalMultiSelectByKey()
    createEavGlobalMultiSelect()
    updateEavGlobalMultiSelect()
    deleteEavGlobalMultiSelect()

    getAllEavGlobal()
    getEavGlobalByKey()
    createEavGlobal()
    updateEavGlobal()
    deleteEavGlobal()

    getALlEavGlobalInfo()
    getEavGlobalInfoByKey()
  }

  private fun getAllEavGlobalBool() {
    val getAllEavGlobalBoolConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobalBool")
    getAllEavGlobalBoolConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_bool"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalBools: MutableList<EavGlobalBool> = emptyList<EavGlobalBool>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalBools.add(makeEavGlobalBool(row))
          }
        }

        message.reply(eavGlobalBools, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalBoolByKey() {
    val getEavGlobalBoolByKeyConsumer = eventBus.consumer<EavGlobalBool>("process.eavGlobal.getEavGlobalBoolByKey")
    getEavGlobalBoolByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_bool WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobalBool(rows.first()), eavGlobalBoolDeliveryOptions)
        } else {
          message.reply("No global bool found")
        }
      }
    }
  }

  private fun createEavGlobalBool() {
    val createEavGlobalBoolConsumer = eventBus.consumer<EavGlobalBool>("process.eavGlobal.createEavGlobalBool")
    createEavGlobalBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_bool (product_id, attribute_key, value) VALUES (?, ?, ?::bit(1))"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalBoolTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalBoolDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalBool() {
    val updateEavGlobalBoolConsumer = eventBus.consumer<EavGlobalBool>("process.eavGlobal.updateEavGlobalBool")
    updateEavGlobalBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_bool SET value =?::bit(1) WHERE product_id =? AND attribute_key =? "
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalBoolTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalBoolDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalBool() {
    val deleteEavGlobalBoolConsumer = eventBus.consumer<EavGlobalBool>("process.eavGlobal.deleteEavGlobalBool")
    deleteEavGlobalBoolConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_bool WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global bool deleted successfully")
      }
    }
  }

  private fun getAllEavGlobalFloat() {
    val allEavGlobalFloatConsumer = eventBus.consumer<JsonObject>("process.eavGlobal.getAllEavGlobalFloat")
    allEavGlobalFloatConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_float"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalFloats: MutableList<EavGlobalFloat> = emptyList<EavGlobalFloat>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalFloats.add(makeEavGlobalFloat(row))
          }
        }

        message.reply(eavGlobalFloats, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalFloatByKey() {
    val getEavGlobalFloatByKeyConsumer = eventBus.consumer<EavGlobalFloat>("process.eavGlobal.getEavGlobalFloatByKey")
    getEavGlobalFloatByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_float WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobalFloat(rows.first()), eavGlobalFloatDeliveryOptions)
        } else {
          message.reply("No global float found")
        }
      }
    }
  }

  private fun createEavGlobalFloat() {
    val createEavGlobalFloatConsumer = eventBus.consumer<EavGlobalFloat>("process.eavGlobal.createEavGlobalFloat")
    createEavGlobalFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_float (product_id, attribute_key, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalFloatTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalFloatDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalFloat() {
    val updateEavGlobalFloatConsumer = eventBus.consumer<EavGlobalFloat>("process.eavGlobal.updateEavGlobalFloat")
    updateEavGlobalFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_float SET value =? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalFloatTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalFloatDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalFloat() {
    val deleteEavGlobalFloatConsumer = eventBus.consumer<EavGlobalFloat>("process.eavGlobal.deleteEavGlobalFloat")
    deleteEavGlobalFloatConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_float WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global float deleted successfully")
      }
    }
  }

  private fun getAllEavGlobalString() {
    val allEavGlobalStringConsumer = eventBus.consumer<JsonObject>("process.eavGlobal.getAllEavGlobalString")
    allEavGlobalStringConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_string"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalStrings: MutableList<EavGlobalString> = emptyList<EavGlobalString>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalStrings.add(makeEavGlobalString(row))
          }
        }

        message.reply(eavGlobalStrings, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalStringByKey() {
    val getEavGlobalStringByKeyConsumer = eventBus.consumer<EavGlobalString>("process.eavGlobal.getEavGlobalStringByKey")
    getEavGlobalStringByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_string WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, eavGlobalStringDeliveryOptions)
        } else {
          message.reply("No global string found")
        }
      }
    }
  }

  private fun createEavGlobalString() {
    val createEavGlobalStringConsumer = eventBus.consumer<EavGlobalString>("process.eavGlobal.createEavGlobalString")
    createEavGlobalStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_string (product_id, attribute_key, value) VALUES (?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalStringTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalStringDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalString() {
    val updateEavGlobalStringConsumer = eventBus.consumer<EavGlobalString>("process.eavGlobal.updateEavGlobalString")
    updateEavGlobalStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_string SET value =? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalStringTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalStringDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalString() {
    val deleteEavGlobalStringConsumer = eventBus.consumer<EavGlobalString>("process.eavGlobal.deleteEavGlobalString")
    deleteEavGlobalStringConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_string WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global string deleted successfully")
      }
    }
  }

  private fun getAllEavGlobalInt() {
    val allEavGlobalIntConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobalInt")
    allEavGlobalIntConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_int"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalInts: MutableList<EavGlobalInt> = emptyList<EavGlobalInt>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalInts.add(makeEavGlobalInt(row))
          }
        }

        message.reply(eavGlobalInts, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalIntByKey() {
    val getEavGlobalIntByKeyConsumer = eventBus.consumer<EavGlobalInt>("process.eavGlobal.getEavGlobalIntByKey")
    getEavGlobalIntByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_int WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobalInt(rows.first()), eavGlobalIntDeliveryOptions)
        } else {
          message.reply("No rows returned")
        }
      }
    }
  }

  private fun createEavGlobalInt() {
    val createEavGlobalIntConsumer = eventBus.consumer<EavGlobalInt>("process.eavGlobal.createEavGlobalInt")
    createEavGlobalIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_int (product_id, attribute_key, value) VALUES (?,?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalIntTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalIntDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalInt() {
    val updateEavGlobalIntConsumer = eventBus.consumer<EavGlobalInt>("process.eavGlobal.updateEavGlobalInt")
    updateEavGlobalIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_int SET value =? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalIntTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalIntDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalInt() {
    val deleteEavGlobalIntConsumer = eventBus.consumer<EavGlobalInt>("process.eavGlobal.deleteEavGlobalInt")
    deleteEavGlobalIntConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_int WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global int deleted successfully")
      }
    }
  }

  private fun getAllEavGlobalMoney() {
    val allEavGlobalMoneyConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobalMoney")
    allEavGlobalMoneyConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_money"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalMoneys: MutableList<EavGlobalMoney> = emptyList<EavGlobalMoney>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalMoneys.add(makeEavGlobalMoney(row))
          }
        }

        message.reply(eavGlobalMoneys, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalMoneyByKey() {
    val getEavGlobalMoneyByKeyConsumer = eventBus.consumer<EavGlobalMoney>("process.eavGlobal.getEavGlobalMoneyByKey")
    getEavGlobalMoneyByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_money WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobalMoney(rows.first()), eavGlobalMoneyDeliveryOptions)
        } else {
          message.reply("No Eav Global Money found")
        }
      }
    }
  }

  private fun createEavGlobalMoney() {
    val createEavGlobalMoneyConsumer = eventBus.consumer<EavGlobalMoney>("process.eavGlobal.createEavGlobalMoney")
    createEavGlobalMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_money (product_id, attribute_key, value) VALUES (?,?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalMoneyTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalMoneyDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalMoney() {
    val updateEavGlobalMoneyConsumer = eventBus.consumer<EavGlobalMoney>("process.eavGlobal.updateEavGlobalMoney")
    updateEavGlobalMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_money SET value =? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalMoneyTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalMoneyDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalMoney() {
    val deleteEavGlobalMoneyConsumer = eventBus.consumer<EavGlobalMoney>("process.eavGlobal.deleteEavGlobalMoney")
    deleteEavGlobalMoneyConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_money WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global money deleted successfully")
      }
    }
  }

  private fun getAllEavGlobalMultiSelect() {
    val allEavGlobalMultiSelectConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobalMultiSelect")
    allEavGlobalMultiSelectConsumer.handler { message ->
      val query = "SELECT * FROM eav_global_multi_select"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalMultiSelects: MutableList<EavGlobalMultiSelect> = emptyList<EavGlobalMultiSelect>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalMultiSelects.add(makeEavGlobalMultiSelect(row))
          }
        }

        message.reply(eavGlobalMultiSelects, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalMultiSelectByKey() {
    val getEavGlobalMultiSelectByKeyConsumer = eventBus.consumer<EavGlobalMultiSelect>("process.eavGlobal.getEavGlobalMultiSelectByKey")
    getEavGlobalMultiSelectByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_global_multi_select WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobalMultiSelect(rows.first()), eavGlobalMultiSelectDeliveryOptions)
        }
      }
    }
  }

  private fun createEavGlobalMultiSelect() {
    val createEavGlobalMultiSelectConsumer = eventBus.consumer<EavGlobalMultiSelect>("process.eavGlobal.createEavGlobalMultiSelect")
    createEavGlobalMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_global_multi_select (product_id, attribute_key, value) VALUES (?,?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalMultiSelectTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalMultiSelectDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobalMultiSelect() {
    val updateEavGlobalMultiSelectConsumer = eventBus.consumer<EavGlobalMultiSelect>("process.eavGlobal.updateEavGlobalMultiSelect")
    updateEavGlobalMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_global_multi_select SET value =? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalMultiSelectTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavGlobalMultiSelectDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobalMultiSelect() {
    val deleteEavGlobalMultiSelectConsumer = eventBus.consumer<EavGlobalMultiSelect>("process.eavGlobal.deleteEavGlobalMultiSelect")
    deleteEavGlobalMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_global_multi_select WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global multi-select deleted successfully")
      }
    }
  }

  private fun getAllEavGlobal() {
    val allEavGlobalConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobal")
    allEavGlobalConsumer.handler { message ->
      val query = "SELECT * FROM eav"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavs: MutableList<Eav> = emptyList<Eav>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavs.add(makeEavGlobal(row))
          }
        }

        message.reply(eavs, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalByKey() {
    val getEavGlobalByKeyConsumer = eventBus.consumer<Eav>("process.eavGlobal.getEavGlobalByKey")
    getEavGlobalByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav WHERE product_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavGlobal(rows.first()), eavDeliveryOptions)
        }
      }
    }
  }

  private fun createEavGlobal() {
    val createEavGlobalConsumer = eventBus.consumer<Eav>("process.eavGlobal.createEavGlobal")
    createEavGlobalConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav (product_id, attribute_key) VALUES (?,?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun updateEavGlobal() {
    val updateEavGlobalConsumer = eventBus.consumer<Eav>("process.eavGlobal.updateEavGlobal")
    updateEavGlobalConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav SET product_id =?, attribute_key=? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavGlobalTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun deleteEavGlobal() {
    val deleteEavGlobalConsumer = eventBus.consumer<Eav>("process.eavGlobal.deleteEavGlobal")
    deleteEavGlobalConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV global deleted successfully")
      }
    }
  }

  private fun getALlEavGlobalInfo() {
    val allEavGlobalInfoConsumer = eventBus.consumer<String>("process.eavGlobal.getAllEavGlobalInfo")
    allEavGlobalInfoConsumer.handler { message ->
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "egms.value AS multi_select_value, cpa.attribute_key FROM products " +
        "LEFT JOIN public.eav_global_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_global_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_global_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_global_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_global_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_global_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key "
      val rowsFuture = client.preparedQuery(query).execute()
      val eavGlobalInfoList: MutableList<EavGlobalInfo> = emptyList<EavGlobalInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalInfoList.add(makeEavGlobalInfo(row))
          }
        }

        message.reply(eavGlobalInfoList, listDeliveryOptions)
      }
    }
  }

  private fun getEavGlobalInfoByKey() {
    val getEavGlobalInfoByKeyConsumer = eventBus.consumer<Int>("process.eavGlobal.getEavGlobalInfoByKey")
    getEavGlobalInfoByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "egms.value AS multi_select_value, cpa.attribute_key FROM products " +
        "LEFT JOIN public.eav_global_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_global_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_global_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_global_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_global_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_global_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "WHERE products.product_id =?"

      val eavGlobalInfoList: MutableList<EavGlobalInfo> = emptyList<EavGlobalInfo>().toMutableList()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))
      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavGlobalInfoList.add(makeEavGlobalInfo(row))
          }
        }

        message.reply(eavGlobalInfoList, listDeliveryOptions)
      }
    }
  }

  private fun makeEavGlobalBool(row: Row): EavGlobalBool {
    return EavGlobalBool(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getBoolean("value")
    )
  }

  private fun makeEavGlobalFloat(row: Row): EavGlobalFloat {
    return EavGlobalFloat(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getFloat("value")
    )
  }

  private fun makeEavGlobalString(row: Row): EavGlobalString {
    return EavGlobalString(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getString("value")
    )
  }

  private fun makeEavGlobalInt(row: Row): EavGlobalInt {
    return EavGlobalInt(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getInteger("value")
    )
  }

  private fun makeEavGlobalMoney(row: Row): EavGlobalMoney {
    return EavGlobalMoney(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getDouble("value")
    )
  }

  private fun makeEavGlobalMultiSelect(row: Row): EavGlobalMultiSelect {
    return EavGlobalMultiSelect(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key"),
      value = row.getInteger("value")
    )
  }

  private fun makeEavGlobal(row: Row): Eav {
    return Eav(
      productId = row.getInteger("product_id"),
      attributeKey = row.getString("attribute_key")
    )
  }

  private fun makeEavGlobalInfo(row: Row): EavGlobalInfo {
    var boolValue: Boolean? = null
    var floatValue: Float? = null
    var intValue: Int? = null
    var stringValue: String? = null
    var moneyValue: Double? = null
    var multiSelectValue: Int? = null

    try {
      boolValue = row.getBoolean("bool_value")
    } catch (_: Exception) {}

    try {
      floatValue = row.getFloat("float_value")
    } catch (_: Exception) {}

    try {
      intValue = row.getInteger("int_value")
    } catch (_: Exception) {}

    try {
      stringValue = row.getString("string_value")
    } catch (_: Exception) {}

    try {
      moneyValue = row.getDouble("money_value")
    } catch (_: Exception) {}

    try {
      multiSelectValue = row.getInteger("multi_select_value")
    } catch (_: Exception) {}

    return EavGlobalInfo(
      eav = makeEavGlobal(row),
      eavGlobalBool = boolValue,
      eavGlobalFloat = floatValue,
      eavGlobalString = stringValue,
      eavGlobalInt = intValue,
      eavGlobalMoney = moneyValue,
      eavGlobalMultiSelect = multiSelectValue
    )
  }

  private fun makeEavGlobalBoolTuple(body: EavGlobalBool, isPutRequest: Boolean): Tuple {
    val eavGlobalBoolTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value.toInt(),
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value.toInt(),
      )
    }

    return eavGlobalBoolTuple
  }

  private fun makeEavGlobalFloatTuple(body: EavGlobalFloat, isPutRequest: Boolean): Tuple {
    val eavGlobalFloatTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value,
      )
    }

    return eavGlobalFloatTuple
  }

  private fun makeEavGlobalStringTuple(body: EavGlobalString, isPutRequest: Boolean): Tuple {
    val eavGlobalStringTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value,
      )
    }

    return eavGlobalStringTuple
  }

  private fun makeEavGlobalIntTuple(body: EavGlobalInt, isPutRequest: Boolean): Tuple {
    val eavGlobalIntTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value,
      )
    }

    return eavGlobalIntTuple
  }

  private fun makeEavGlobalMoneyTuple(body: EavGlobalMoney, isPutRequest: Boolean): Tuple {
    val eavGlobalMoneyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value,
      )
    }

    return eavGlobalMoneyTuple
  }

  private fun makeEavGlobalMultiSelectTuple(body: EavGlobalMultiSelect, isPutRequest: Boolean): Tuple {
    val eavGlobalMultiSelectTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.value,
      )
    }

    return eavGlobalMultiSelectTuple
  }

  private fun makeEavGlobalTuple(body: Eav, isPutRequest: Boolean): Tuple {
    val eavGlobalTuple = if (isPutRequest) {
      Tuple.of(
        body.productId,
        body.attributeKey,
        body.productId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.attributeKey,
      )
    }

    return eavGlobalTuple
  }

  private fun Boolean.toInt() = if (this) 1 else 0
}
