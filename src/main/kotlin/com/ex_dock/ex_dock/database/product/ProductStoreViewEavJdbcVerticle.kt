package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductStoreViewEavJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val eavStoreViewBoolDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewBoolCodec")
  private val eavStoreViewFloatDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewFloatCodec")
  private val eavStoreViewIntDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewIntCodec")
  private val eavStoreViewMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewMoneyCodec")
  private val eavStoreViewMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewMultiSelectCodec")
  private val eavStoreViewStringDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewStringCodec")
  private val eavStoreViewInfoDeliveryOptions = DeliveryOptions().setCodecName("EavStoreViewInfoCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")


  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    getAllEavStoreViewBool()
    getEavStoreViewBoolByKey()
    createEavStoreViewBool()
    updateEavStoreViewBool()
    deleteEavStoreViewBool()

    getAllEavStoreViewFloat()
    getEavStoreViewFloatByKey()
    createEavStoreViewFloat()
    updateEavStoreViewFloat()
    deleteEavStoreViewFloat()

    getAllEavStoreViewString()
    getEavStoreViewStringByKey()
    createEavStoreViewString()
    updateEavStoreViewString()
    deleteEavStoreViewString()

    getAllEavStoreViewInt()
    getEavStoreViewIntByKey()
    createEavStoreViewInt()
    updateEavStoreViewInt()
    deleteEavStoreViewInt()

    getAllEavStoreViewMoney()
    getEavStoreViewMoneyByKey()
    createEavStoreViewMoney()
    updateEavStoreViewMoney()
    deleteEavStoreViewMoney()

    getAllEavStoreViewMultiSelect()
    getEavStoreViewMultiSelectByKey()
    createEavStoreViewMultiSelect()
    updateEavStoreViewMultiSelect()
    deleteEavStoreViewMultiSelect()

    getAllEavStoreView()
    getEavStoreViewByKey()
    createEavStoreView()
    updateEavStoreView()
    deleteEavStoreView()

    getALlEavStoreViewInfo()
    getEavStoreViewInfoByKey()
  }

  private fun getAllEavStoreViewBool() {
    val getAllEavStoreViewBoolConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreViewBool")
    getAllEavStoreViewBoolConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_bool"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewBoolList: MutableList<EavStoreViewBool> = emptyList<EavStoreViewBool>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewBoolList.add(makeEavStoreViewBool(row))
          }
        }

        message.reply(eavStoreViewBoolList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewBoolByKey() {
    val getEavStoreViewBoolByKeyConsumer = eventBus.consumer<EavStoreViewBool>("process.eavStoreView.getEavStoreViewBoolByKey")
    getEavStoreViewBoolByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_bool WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(body, eavStoreViewBoolDeliveryOptions)
        } else {
          message.reply("No storeView bool found")
        }
      }
    }
  }

  private fun createEavStoreViewBool() {
    val createEavStoreViewBoolConsumer = eventBus.consumer<EavStoreViewBool>("process.eavStoreView.createEavStoreViewBool")
    createEavStoreViewBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_bool (product_id, store_view_id, attribute_key, value) VALUES (?, ?, ?, ?::bit(1))"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewBoolTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewBoolDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewBool() {
    val updateEavStoreViewBoolConsumer = eventBus.consumer<EavStoreViewBool>("process.eavStoreView.updateEavStoreViewBool")
    updateEavStoreViewBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_bool SET value =?::bit(1) WHERE product_id =? AND store_view_id =? AND attribute_key =? "
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewBoolTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewBoolDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewBool() {
    val deleteEavStoreViewBoolConsumer = eventBus.consumer<EavStoreViewBool>("process.eavStoreView.deleteEavStoreViewBool")
    deleteEavStoreViewBoolConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_bool WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView bool deleted successfully")
      }
    }
  }

  private fun getAllEavStoreViewFloat() {
    val allEavStoreViewFloatConsumer = eventBus.consumer<JsonObject>("process.eavStoreView.getAllEavStoreViewFloat")
    allEavStoreViewFloatConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_float"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewFloatList: MutableList<EavStoreViewFloat> = emptyList<EavStoreViewFloat>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewFloatList.add(makeEavStoreViewFloat(row))
          }
        }

        message.reply(eavStoreViewFloatList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewFloatByKey() {
    val getEavStoreViewFloatByKeyConsumer = eventBus.consumer<EavStoreViewFloat>("process.eavStoreView.getEavStoreViewFloatByKey")
    getEavStoreViewFloatByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_float WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavStoreViewFloat(rows.first()), eavStoreViewFloatDeliveryOptions)
        } else {
          message.reply("No storeView float found")
        }
      }
    }
  }

  private fun createEavStoreViewFloat() {
    val createEavStoreViewFloatConsumer = eventBus.consumer<EavStoreViewFloat>("process.eavStoreView.createEavStoreViewFloat")
    createEavStoreViewFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_float (product_id, store_view_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewFloatTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewFloatDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewFloat() {
    val updateEavStoreViewFloatConsumer = eventBus.consumer<EavStoreViewFloat>("process.eavStoreView.updateEavStoreViewFloat")
    updateEavStoreViewFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_float SET value =? WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewFloatTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewFloatDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewFloat() {
    val deleteEavStoreViewFloatConsumer = eventBus.consumer<EavStoreViewFloat>("process.eavStoreView.deleteEavStoreViewFloat")
    deleteEavStoreViewFloatConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_float WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView float deleted successfully")
      }
    }
  }

  private fun getAllEavStoreViewString() {
    val allEavStoreViewStringConsumer = eventBus.consumer<JsonObject>("process.eavStoreView.getAllEavStoreViewString")
    allEavStoreViewStringConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_string"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewStringList: MutableList<EavStoreViewString> = emptyList<EavStoreViewString>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewStringList.add(makeEavStoreViewString(row))
          }
        }

        message.reply(eavStoreViewStringList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewStringByKey() {
    val getEavStoreViewStringByKeyConsumer = eventBus.consumer<EavStoreViewString>("process.eavStoreView.getEavStoreViewStringByKey")
    getEavStoreViewStringByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_string WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavStoreViewString(rows.first()), eavStoreViewStringDeliveryOptions)
        } else {
          message.reply("No storeView string found")
        }
      }
    }
  }

  private fun createEavStoreViewString() {
    val createEavStoreViewStringConsumer = eventBus.consumer<EavStoreViewString>("process.eavStoreView.createEavStoreViewString")
    createEavStoreViewStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_string (product_id, store_view_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewStringTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewStringDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewString() {
    val updateEavStoreViewStringConsumer = eventBus.consumer<EavStoreViewString>("process.eavStoreView.updateEavStoreViewString")
    updateEavStoreViewStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_string SET value =? WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewStringTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewStringDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewString() {
    val deleteEavStoreViewStringConsumer = eventBus.consumer<EavStoreViewString>("process.eavStoreView.deleteEavStoreViewString")
    deleteEavStoreViewStringConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_string WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView string deleted successfully")
      }
    }
  }

  private fun getAllEavStoreViewInt() {
    val allEavStoreViewIntConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreViewInt")
    allEavStoreViewIntConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_int"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewIntList: MutableList<EavStoreViewInt> = emptyList<EavStoreViewInt>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach {row ->
            eavStoreViewIntList.add(makeEavStoreViewInt(row))
          }
        }

        message.reply(eavStoreViewIntList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewIntByKey() {
    val getEavStoreViewIntByKeyConsumer = eventBus.consumer<EavStoreViewInt>("process.eavStoreView.getEavStoreViewIntByKey")
    getEavStoreViewIntByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_int WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavStoreViewInt(rows.first()), eavStoreViewIntDeliveryOptions)
        } else {
          message.reply("No rows returned")
        }
      }
    }
  }

  private fun createEavStoreViewInt() {
    val createEavStoreViewIntConsumer = eventBus.consumer<EavStoreViewInt>("process.eavStoreView.createEavStoreViewInt")
    createEavStoreViewIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_int (product_id, store_view_id ,attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewIntTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewIntDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewInt() {
    val updateEavStoreViewIntConsumer = eventBus.consumer<EavStoreViewInt>("process.eavStoreView.updateEavStoreViewInt")
    updateEavStoreViewIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_int SET value =? WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewIntTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewIntDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewInt() {
    val deleteEavStoreViewIntConsumer = eventBus.consumer<EavStoreViewInt>("process.eavStoreView.deleteEavStoreViewInt")
    deleteEavStoreViewIntConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_int WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView int deleted successfully")
      }
    }
  }

  private fun getAllEavStoreViewMoney() {
    val allEavStoreViewMoneyConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreViewMoney")
    allEavStoreViewMoneyConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_money"
      val rowsFuture = client.preparedQuery(query).execute()
      val allEavStoreViewMoneyList: MutableList<EavStoreViewMoney> = emptyList<EavStoreViewMoney>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            allEavStoreViewMoneyList.add(makeEavStoreViewMoney(row))
          }
        }

        message.reply(allEavStoreViewMoneyList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewMoneyByKey() {
    val getEavStoreViewMoneyByKeyConsumer = eventBus.consumer<EavStoreViewMoney>("process.eavStoreView.getEavStoreViewMoneyByKey")
    getEavStoreViewMoneyByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_money WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavStoreViewMoney(rows.first()), eavStoreViewMoneyDeliveryOptions)
        } else {
          message.reply("No Eav Global Money found")
        }
      }
    }
  }

  private fun createEavStoreViewMoney() {
    val createEavStoreViewMoneyConsumer = eventBus.consumer<EavStoreViewMoney>("process.eavStoreView.createEavStoreViewMoney")
    createEavStoreViewMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_money (product_id, store_view_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewMoneyTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewMoneyDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewMoney() {
    val updateEavStoreViewMoneyConsumer = eventBus.consumer<EavStoreViewMoney>("process.eavStoreView.updateEavStoreViewMoney")
    updateEavStoreViewMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_money SET value =? WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewMoneyTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewMoneyDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewMoney() {
    val deleteEavStoreViewMoneyConsumer = eventBus.consumer<EavStoreViewMoney>("process.eavStoreView.deleteEavStoreViewMoney")
    deleteEavStoreViewMoneyConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_money WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView money deleted successfully")
      }
    }
  }

  private fun getAllEavStoreViewMultiSelect() {
    val allEavStoreViewMultiSelectConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreViewMultiSelect")
    allEavStoreViewMultiSelectConsumer.handler { message ->
      val query = "SELECT * FROM eav_store_view_multi_select"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewMultiSelectList: MutableList<EavStoreViewMultiSelect> = emptyList<EavStoreViewMultiSelect>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewMultiSelectList.add(makeEavStoreViewMultiSelect(row))
          }
        }

        message.reply(eavStoreViewMultiSelectList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewMultiSelectByKey() {
    val getEavStoreViewMultiSelectByKeyConsumer = eventBus.consumer<EavStoreViewMultiSelect>("process.eavStoreView.getEavStoreViewMultiSelectByKey")
    getEavStoreViewMultiSelectByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_store_view_multi_select WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeEavStoreViewMultiSelect(rows.first()), eavStoreViewMultiSelectDeliveryOptions)
        } else {
          message.reply("No EAV storeView multi-select found")
        }
      }
    }
  }

  private fun createEavStoreViewMultiSelect() {
    val createEavStoreViewMultiSelectConsumer = eventBus.consumer<EavStoreViewMultiSelect>("process.eavStoreView.createEavStoreViewMultiSelect")
    createEavStoreViewMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_store_view_multi_select (product_id, store_view_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewMultiSelectTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewMultiSelectDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreViewMultiSelect() {
    val updateEavStoreViewMultiSelectConsumer = eventBus.consumer<EavStoreViewMultiSelect>("process.eavStoreView.updateEavStoreViewMultiSelect")
    updateEavStoreViewMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_store_view_multi_select SET value =? WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewMultiSelectTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavStoreViewMultiSelectDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreViewMultiSelect() {
    val deleteEavStoreViewMultiSelectConsumer = eventBus.consumer<EavStoreViewMultiSelect>("process.eavStoreView.deleteEavStoreViewMultiSelect")
    deleteEavStoreViewMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_store_view_multi_select WHERE product_id =? AND store_view_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.storeViewId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView multi-select deleted successfully")
      }
    }
  }

  private fun getAllEavStoreView() {
    val allEavStoreViewConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreView")
    allEavStoreViewConsumer.handler { message ->
      val query = "SELECT * FROM eav"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavList: MutableList<Eav> = emptyList<Eav>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavList.add(makeEavStoreView(row))
          }
        }

        message.reply(eavList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewByKey() {
    val getEavStoreViewByKeyConsumer = eventBus.consumer<Eav>("process.eavStoreView.getEavStoreViewByKey")
    getEavStoreViewByKeyConsumer.handler { message ->
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
          message.reply(makeEavStoreView(rows.first()), eavDeliveryOptions)
        } else {
          message.reply("No EAV storeView found")
        }
      }
    }
  }

  private fun createEavStoreView() {
    val createEavStoreViewConsumer = eventBus.consumer<Eav>("process.eavStoreView.createEavStoreView")
    createEavStoreViewConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav (product_id, attribute_key) VALUES (?,?)"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun updateEavStoreView() {
    val updateEavStoreViewConsumer = eventBus.consumer<Eav>("process.eavStoreView.updateEavStoreView")
    updateEavStoreViewConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav SET product_id =?, attribute_key=? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(makeEavStoreViewTuple(body, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun deleteEavStoreView() {
    val deleteEavStoreViewConsumer = eventBus.consumer<Eav>("process.eavStoreView.deleteEavStoreView")
    deleteEavStoreViewConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        message.reply("EAV storeView deleted successfully")
      }
    }
  }

  private fun getALlEavStoreViewInfo() {
    val allEavStoreViewInfoConsumer = eventBus.consumer<String>("process.eavStoreView.getAllEavStoreViewInfo")
    allEavStoreViewInfoConsumer.handler { message ->
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, products.short_description, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "products.sku, products.ean, products.manufacturer, " +
        "egms.value AS multi_select_value, cpa.attribute_key, sv.store_view_id AS store_view_id FROM products " +
        "LEFT JOIN public.eav_store_view_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_store_view_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_store_view_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_store_view_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_store_view_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_store_view_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.store_view sv on egb.store_view_id = sv.store_view_id "
      val rowsFuture = client.preparedQuery(query).execute()
      val eavStoreViewInfoList: MutableList<EavStoreViewInfo> = emptyList<EavStoreViewInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewInfoList.add(makeEavStoreViewInfo(row))
          }
        }

        message.reply(eavStoreViewInfoList, listDeliveryOptions)
      }
    }
  }

  private fun getEavStoreViewInfoByKey() {
    val getEavStoreViewInfoByKeyConsumer = eventBus.consumer<Int>("process.eavStoreView.getEavStoreViewInfoByKey")
    getEavStoreViewInfoByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, products.short_description, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "products.sku, products.ean, products.manufacturer, " +
        "egms.value AS multi_select_value, cpa.attribute_key, sv.store_view_id AS store_view_id FROM products " +
        "LEFT JOIN public.eav_store_view_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_store_view_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_store_view_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_store_view_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_store_view_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_store_view_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.store_view sv on egb.store_view_id = sv.store_view_id " +
        "WHERE products.product_id =?"
      val eavStoreViewInfoList: MutableList<EavStoreViewInfo> = emptyList<EavStoreViewInfo>().toMutableList()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))
      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavStoreViewInfoList.add(makeEavStoreViewInfo(row))
          }
        }

        message.reply(eavStoreViewInfoList, listDeliveryOptions)
      }
    }
  }

  private fun makeEavStoreViewBool(row: Row): EavStoreViewBool {
    return EavStoreViewBool(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getBoolean("value")
    )
  }

  private fun makeEavStoreViewFloat(row: Row): EavStoreViewFloat {
    return EavStoreViewFloat(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getFloat("value")
    )
  }

  private fun makeEavStoreViewString(row: Row): EavStoreViewString {
    return EavStoreViewString(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getString("value")
    )
  }

  private fun makeEavStoreViewInt(row: Row): EavStoreViewInt {
    return EavStoreViewInt(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getInteger("value")
    )
  }

  private fun makeEavStoreViewMoney(row: Row): EavStoreViewMoney {
    return EavStoreViewMoney(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getDouble("value")
    )
  }

  private fun makeEavStoreViewMultiSelect(row: Row): EavStoreViewMultiSelect {
    return EavStoreViewMultiSelect(
      row.getInteger("product_id"),
      row.getInteger("store_view_id"),
      row.getString("attribute_key"),
      row.getInteger("value")
    )
  }

  private fun makeEavStoreView(row: Row): Eav {
    return Eav(
      row.getInteger("product_id"),
      row.getString("attribute_key")
    )
  }

  private fun makeEavStoreViewInfo(row: Row): EavStoreViewInfo {
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

    return EavStoreViewInfo(
      Products(
        row.getInteger("product_id"),
        row.getString("name"),
        row.getString("short_name"),
        row.getString("description"),
        row.getString("short_description"),
        row.getString("sku"),
        row.getString("ean"),
        row.getString("manufacturer")
      ),
      row.getString("attribute_key"),
      boolValue,
      floatValue,
      intValue,
      stringValue,
      multiSelectValue,
      moneyValue,
    )
  }

  private fun makeEavStoreViewBoolTuple(body: EavStoreViewBool, isPutRequest: Boolean): Tuple {
    val eavStoreViewBoolTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value.toInt(),
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value.toInt(),
      )
    }

    return eavStoreViewBoolTuple
  }

  private fun makeEavStoreViewFloatTuple(body: EavStoreViewFloat, isPutRequest: Boolean): Tuple {
    val eavStoreViewFloatTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value,
      )
    }

    return eavStoreViewFloatTuple
  }

  private fun makeEavStoreViewStringTuple(body: EavStoreViewString, isPutRequest: Boolean): Tuple {
    val eavStoreViewStringTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value,
      )
    }

    return eavStoreViewStringTuple
  }

  private fun makeEavStoreViewIntTuple(body: EavStoreViewInt, isPutRequest: Boolean): Tuple {
    val eavStoreViewIntTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value,
      )
    }

    return eavStoreViewIntTuple
  }

  private fun makeEavStoreViewMoneyTuple(body: EavStoreViewMoney, isPutRequest: Boolean): Tuple {
    val eavStoreViewMoneyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value,
      )
    }

    return eavStoreViewMoneyTuple
  }

  private fun makeEavStoreViewMultiSelectTuple(body: EavStoreViewMultiSelect, isPutRequest: Boolean): Tuple {
    val eavStoreViewMultiSelectTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.value,
        body.productId,
        body.storeViewId,
        body.attributeKey,
      )
    } else {
      Tuple.of(
        body.productId,
        body.storeViewId,
        body.attributeKey,
        body.value,
      )
    }

    return eavStoreViewMultiSelectTuple
  }

  private fun makeEavStoreViewTuple(body: Eav, isPutRequest: Boolean): Tuple {
    val eavStoreViewTuple = if (isPutRequest) {
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

    return eavStoreViewTuple
  }

  private fun Boolean.toInt() = if (this) 1 else 0
}
