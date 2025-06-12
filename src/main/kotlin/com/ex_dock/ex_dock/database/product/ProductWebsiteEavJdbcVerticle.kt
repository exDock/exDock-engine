package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductWebsiteEavJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val eavWebsiteBoolDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteBoolCodec")
  private val eavWebsiteFloatDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteFloatCodec")
  private val eavWebsiteIntDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteIntCodec")
  private val eavWebsiteMoneyDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteMoneyCodec")
  private val eavWebsiteMultiSelectDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteMultiSelectCodec")
  private val eavWebsiteStringDeliveryOptions = DeliveryOptions().setCodecName("EavWebsiteStringCodec")
  private val eavDeliveryOptions = DeliveryOptions().setCodecName("EavCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  companion object {
    private const val CACHE_ADDRESS = "website_eav"
  }

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    getAllEavWebsiteBool()
    getEavWebsiteBoolByKey()
    createEavWebsiteBool()
    updateEavWebsiteBool()
    deleteEavWebsiteBool()

    getAllEavWebsiteFloat()
    getEavWebsiteFloatByKey()
    createEavWebsiteFloat()
    updateEavWebsiteFloat()
    deleteEavWebsiteFloat()

    getAllEavWebsiteString()
    getEavWebsiteStringByKey()
    createEavWebsiteString()
    updateEavWebsiteString()
    deleteEavWebsiteString()

    getAllEavWebsiteInt()
    getEavWebsiteIntByKey()
    createEavWebsiteInt()
    updateEavWebsiteInt()
    deleteEavWebsiteInt()

    getAllEavWebsiteMoney()
    getEavWebsiteMoneyByKey()
    createEavWebsiteMoney()
    updateEavWebsiteMoney()
    deleteEavWebsiteMoney()

    getAllEavWebsiteMultiSelect()
    getEavWebsiteMultiSelectByKey()
    createEavWebsiteMultiSelect()
    updateEavWebsiteMultiSelect()
    deleteEavWebsiteMultiSelect()

    getAllEavWebsite()
    getEavWebsiteByKey()
    createEavWebsite()
    updateEavWebsite()
    deleteEavWebsite()

    getALlEavWebsiteInfo()
    getEavWebsiteInfoByKey()
  }

  private fun getAllEavWebsiteBool() {
    val getAllEavWebsiteBoolConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsiteBool")
    getAllEavWebsiteBoolConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_bool"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteBoolList: MutableList<EavWebsiteBool> = emptyList<EavWebsiteBool>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteBoolList.add(row.makeEavWebsiteBool())
          }
        }

        message.reply(eavWebsiteBoolList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteBoolByKey() {
    val getEavWebsiteBoolByKeyConsumer = eventBus.consumer<EavWebsiteBool>("process.eavWebsite.getEavWebsiteBoolByKey")
    getEavWebsiteBoolByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_bool WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteBool(), eavWebsiteBoolDeliveryOptions)
        } else {
          message.reply("No website bool found")
        }
      }
    }
  }

  private fun createEavWebsiteBool() {
    val createEavWebsiteBoolConsumer = eventBus.consumer<EavWebsiteBool>("process.eavWebsite.createEavWebsiteBool")
    createEavWebsiteBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_bool (product_id, website_id, attribute_key, value) VALUES (?, ?, ?, ?::bit(1))"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteBoolDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteBool() {
    val updateEavWebsiteBoolConsumer = eventBus.consumer<EavWebsiteBool>("process.eavWebsite.updateEavWebsiteBool")
    updateEavWebsiteBoolConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_bool SET value =?::bit(1) WHERE product_id =? AND website_id =? AND attribute_key =? "
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteBoolDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteBool() {
    val deleteEavWebsiteBoolConsumer = eventBus.consumer<EavWebsiteBool>("process.eavWebsite.deleteEavWebsiteBool")
    deleteEavWebsiteBoolConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_bool WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website bool deleted successfully")
      }
    }
  }

  private fun getAllEavWebsiteFloat() {
    val allEavWebsiteFloatConsumer = eventBus.consumer<JsonObject>("process.eavWebsite.getAllEavWebsiteFloat")
    allEavWebsiteFloatConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_float"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteFloats: MutableList<EavWebsiteFloat> = emptyList<EavWebsiteFloat>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteFloats.add(row.makeEavWebsiteFloat())
          }
        }

        message.reply(eavWebsiteFloats, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteFloatByKey() {
    val getEavWebsiteFloatByKeyConsumer = eventBus.consumer<EavWebsiteFloat>("process.eavWebsite.getEavWebsiteFloatByKey")
    getEavWebsiteFloatByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_float WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteFloat(), eavWebsiteFloatDeliveryOptions)
        } else {
          message.reply("No website float found")
        }
      }
    }
  }

  private fun createEavWebsiteFloat() {
    val createEavWebsiteFloatConsumer = eventBus.consumer<EavWebsiteFloat>("process.eavWebsite.createEavWebsiteFloat")
    createEavWebsiteFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_float (product_id, website_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteFloatDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteFloat() {
    val updateEavWebsiteFloatConsumer = eventBus.consumer<EavWebsiteFloat>("process.eavWebsite.updateEavWebsiteFloat")
    updateEavWebsiteFloatConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_float SET value =? WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteFloatDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteFloat() {
    val deleteEavWebsiteFloatConsumer = eventBus.consumer<EavWebsiteFloat>("process.eavWebsite.deleteEavWebsiteFloat")
    deleteEavWebsiteFloatConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_float WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website float deleted successfully")
      }
    }
  }

  private fun getAllEavWebsiteString() {
    val allEavWebsiteStringConsumer = eventBus.consumer<JsonObject>("process.eavWebsite.getAllEavWebsiteString")
    allEavWebsiteStringConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_string"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteStringList: MutableList<EavWebsiteString> = emptyList<EavWebsiteString>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteStringList.add(row.makeEavWebsiteString())
          }
        }

        message.reply(eavWebsiteStringList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteStringByKey() {
    val getEavWebsiteStringByKeyConsumer = eventBus.consumer<EavWebsiteString>("process.eavWebsite.getEavWebsiteStringByKey")
    getEavWebsiteStringByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_string WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteString(), eavWebsiteStringDeliveryOptions)
        } else {
          message.reply("No website string found")
        }
      }
    }
  }

  private fun createEavWebsiteString() {
    val createEavWebsiteStringConsumer = eventBus.consumer<EavWebsiteString>("process.eavWebsite.createEavWebsiteString")
    createEavWebsiteStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_string (product_id, website_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteStringDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteString() {
    val updateEavWebsiteStringConsumer = eventBus.consumer<EavWebsiteString>("process.eavWebsite.updateEavWebsiteString")
    updateEavWebsiteStringConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_string SET value =? WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteStringDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteString() {
    val deleteEavWebsiteStringConsumer = eventBus.consumer<EavWebsiteString>("process.eavWebsite.deleteEavWebsiteString")
    deleteEavWebsiteStringConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_string WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website string deleted successfully")
      }
    }
  }

  private fun getAllEavWebsiteInt() {
    val allEavWebsiteIntConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsiteInt")
    allEavWebsiteIntConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_int"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteIntList: MutableList<EavWebsiteInt> = emptyList<EavWebsiteInt>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteIntList.add(row.makeEavWebsiteInt())
          }
        }

        message.reply(eavWebsiteIntList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteIntByKey() {
    val getEavWebsiteIntByKeyConsumer = eventBus.consumer<EavWebsiteInt>("process.eavWebsite.getEavWebsiteIntByKey")
    getEavWebsiteIntByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_int WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteInt(), eavWebsiteIntDeliveryOptions)
        } else {
          message.reply("No rows returned")
        }
      }
    }
  }

  private fun createEavWebsiteInt() {
    val createEavWebsiteIntConsumer = eventBus.consumer<EavWebsiteInt>("process.eavWebsite.createEavWebsiteInt")
    createEavWebsiteIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_int (product_id, website_id ,attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteIntDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteInt() {
    val updateEavWebsiteIntConsumer = eventBus.consumer<EavWebsiteInt>("process.eavWebsite.updateEavWebsiteInt")
    updateEavWebsiteIntConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_int SET value =? WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteIntDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteInt() {
    val deleteEavWebsiteIntConsumer = eventBus.consumer<EavWebsiteInt>("process.eavWebsite.deleteEavWebsiteInt")
    deleteEavWebsiteIntConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_int WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website int deleted successfully")
      }
    }
  }

  private fun getAllEavWebsiteMoney() {
    val allEavWebsiteMoneyConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsiteMoney")
    allEavWebsiteMoneyConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_money"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteMoneyList: MutableList<EavWebsiteMoney> = emptyList<EavWebsiteMoney>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteMoneyList.add(row.makeEavWebsiteMoney())
          }
        }

        message.reply(eavWebsiteMoneyList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteMoneyByKey() {
    val getEavWebsiteMoneyByKeyConsumer = eventBus.consumer<EavWebsiteMoney>("process.eavWebsite.getEavWebsiteMoneyByKey")
    getEavWebsiteMoneyByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_money WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteMoney(), eavWebsiteMoneyDeliveryOptions)
        } else {
          message.reply("No Eav Website Money found")
        }
      }
    }
  }

  private fun createEavWebsiteMoney() {
    val createEavWebsiteMoneyConsumer = eventBus.consumer<EavWebsiteMoney>("process.eavWebsite.createEavWebsiteMoney")
    createEavWebsiteMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_money (product_id, website_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteMoneyDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteMoney() {
    val updateEavWebsiteMoneyConsumer = eventBus.consumer<EavWebsiteMoney>("process.eavWebsite.updateEavWebsiteMoney")
    updateEavWebsiteMoneyConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_money SET value =? WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteMoneyDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteMoney() {
    val deleteEavWebsiteMoneyConsumer = eventBus.consumer<EavWebsiteMoney>("process.eavWebsite.deleteEavWebsiteMoney")
    deleteEavWebsiteMoneyConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_money WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website money deleted successfully")
      }
    }
  }

  private fun getAllEavWebsiteMultiSelect() {
    val allEavWebsiteMultiSelectConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsiteMultiSelect")
    allEavWebsiteMultiSelectConsumer.handler { message ->
      val query = "SELECT * FROM eav_website_multi_select"
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteMultiSelectList: MutableList<EavWebsiteMultiSelect> = emptyList<EavWebsiteMultiSelect>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteMultiSelectList.add(row.makeEavWebsiteMultiSelect())
          }
        }

        message.reply(eavWebsiteMultiSelectList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteMultiSelectByKey() {
    val getEavWebsiteMultiSelectByKeyConsumer = eventBus.consumer<EavWebsiteMultiSelect>("process.eavWebsite.getEavWebsiteMultiSelectByKey")
    getEavWebsiteMultiSelectByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM eav_website_multi_select WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture =
        client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeEavWebsiteMultiSelect(), eavWebsiteMultiSelectDeliveryOptions)
        } else {
          message.reply("No EAV Website Multi-Select found")
        }
      }
    }
  }

  private fun createEavWebsiteMultiSelect() {
    val createEavWebsiteMultiSelectConsumer = eventBus.consumer<EavWebsiteMultiSelect>("process.eavWebsite.createEavWebsiteMultiSelect")
    createEavWebsiteMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav_website_multi_select (product_id, website_id, attribute_key, value) VALUES (?, ?, ?, ?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteMultiSelectDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsiteMultiSelect() {
    val updateEavWebsiteMultiSelectConsumer = eventBus.consumer<EavWebsiteMultiSelect>("process.eavWebsite.updateEavWebsiteMultiSelect")
    updateEavWebsiteMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav_website_multi_select SET value =? WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavWebsiteMultiSelectDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsiteMultiSelect() {
    val deleteEavWebsiteMultiSelectConsumer = eventBus.consumer<EavWebsiteMultiSelect>("process.eavWebsite.deleteEavWebsiteMultiSelect")
    deleteEavWebsiteMultiSelectConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav_website_multi_select WHERE product_id =? AND website_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.websiteId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website multi-select deleted successfully")
      }
    }
  }

  private fun getAllEavWebsite() {
    val allEavWebsiteConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsite")
    allEavWebsiteConsumer.handler { message ->
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
            eavList.add(row.makeEavWebsite())
          }
        }

        message.reply(eavList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteByKey() {
    val getEavWebsiteByKeyConsumer = eventBus.consumer<Eav>("process.eavWebsite.getEavWebsiteByKey")
    getEavWebsiteByKeyConsumer.handler { message ->
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
          message.reply(rows.first().makeEavWebsite(), eavDeliveryOptions)
        } else {
          message.reply("No EAV found")
        }
      }
    }
  }

  private fun createEavWebsite() {
    val createEavWebsiteConsumer = eventBus.consumer<Eav>("process.eavWebsite.createEavWebsite")
    createEavWebsiteConsumer.handler { message ->
      val body = message.body()
      val query =
        "INSERT INTO eav (product_id, attribute_key) VALUES (?,?)"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun updateEavWebsite() {
    val updateEavWebsiteConsumer = eventBus.consumer<Eav>("process.eavWebsite.updateEavWebsite")
    updateEavWebsiteConsumer.handler { message ->
      val body = message.body()
      val query =
        "UPDATE eav SET product_id =?, attribute_key=? WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, eavDeliveryOptions)
      }
    }
  }

  private fun deleteEavWebsite() {
    val deleteEavWebsiteConsumer = eventBus.consumer<Eav>("process.eavWebsite.deleteEavWebsite")
    deleteEavWebsiteConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM eav WHERE product_id =? AND attribute_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.productId, body.attributeKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply("EAV website deleted successfully")
      }
    }
  }

  private fun getALlEavWebsiteInfo() {
    val allEavWebsiteInfoConsumer = eventBus.consumer<String>("process.eavWebsite.getAllEavWebsiteInfo")
    allEavWebsiteInfoConsumer.handler { message ->
      val query = "SELECT products.product_id, products.name, products.short_name, " +
      "products.description, products.short_name, products.short_description, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "products.sku, products.ean, products.manufacturer, " +
        "egms.value AS multi_select_value, cpa.attribute_key AS attribute_key, w.website_id AS website_id FROM products " +
        "LEFT JOIN public.eav_website_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_website_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_website_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_website_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_website_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_website_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "Left JOIN public.websites w ON egb.website_id = w.website_id "
      val rowsFuture = client.preparedQuery(query).execute()
      val eavWebsiteInfoList: MutableList<EavWebsiteInfo> = emptyList<EavWebsiteInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteInfoList.add(row.makeEavWebsiteInfo())
          }
        }

        message.reply(eavWebsiteInfoList, listDeliveryOptions)
      }
    }
  }

  private fun getEavWebsiteInfoByKey() {
    val getEavWebsiteInfoByKeyConsumer = eventBus.consumer<Int>("process.eavWebsite.getEavWebsiteInfoByKey")
    getEavWebsiteInfoByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT products.product_id, products.name, products.short_name, " +
        "products.description, products.short_name, products.short_description, egb.value AS bool_value, " +
        "egf.value AS float_value, egs.value AS string_value, " +
        "egi.value AS int_value, egm.value AS money_value, " +
        "products.sku, products.ean, products.manufacturer, " +
        "egms.value AS multi_select_value, cpa.attribute_key, w.website_id AS website_id FROM products " +
        "LEFT JOIN public.eav_website_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_website_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_website_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_website_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_website_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_website_string egs on products.product_id = egs.product_id " +
        "Left Join public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "Left JOIN public.websites w ON egb.website_id = w.website_id " +
        "WHERE products.product_id =?"

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body))
      val eavWebsiteInfoList: MutableList<EavWebsiteInfo> = emptyList<EavWebsiteInfo>().toMutableList()
      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            eavWebsiteInfoList.add(row.makeEavWebsiteInfo())
          }
        }

        message.reply(eavWebsiteInfoList, listDeliveryOptions)
      }
    }
  }

  private fun Row.makeEavWebsiteBool(): EavWebsiteBool {
    return EavWebsiteBool(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getBoolean("value")
    )
  }

  private fun Row.makeEavWebsiteFloat(): EavWebsiteFloat {
    return EavWebsiteFloat(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getFloat("value")
    )
  }

  private fun Row.makeEavWebsiteString(): EavWebsiteString {
    return EavWebsiteString(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getString("value")
    )
  }

  private fun Row.makeEavWebsiteInt(): EavWebsiteInt {
    return EavWebsiteInt(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getInteger("value")
    )
  }

  private fun Row.makeEavWebsiteMoney(): EavWebsiteMoney {
    return EavWebsiteMoney(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getDouble("value")
    )
  }

  private fun Row.makeEavWebsiteMultiSelect(): EavWebsiteMultiSelect {
    return EavWebsiteMultiSelect(
      this.getInteger("product_id"),
      this.getInteger("website_id"),
      this.getString("attribute_key"),
      this.getInteger("value")
    )
  }

  private fun Row.makeEavWebsite(): Eav {
    return Eav(
      this.getInteger("product_id"),
      this.getString("attribute_key"),
    )
  }

  private fun Row.makeEavWebsiteInfo(): EavWebsiteInfo {
    return EavWebsiteInfo(
      Products(
        this.getInteger("product_id"),
        this.getString("name"),
        this.getString("short_name"),
        this.getString("description"),
        this.getString("short_description"),
        this.getString("sku"),
        this.getString("ean"),
        this.getString("manufacturer")
      ),
      this.getString("attribute_key"),
      try {this.getBoolean("bool_value")} catch (_: Exception) {null},
      try {this.getFloat("float_value")} catch (_: Exception) {null},
      try {this.getString("string_value")} catch (_: Exception) {null},
      try {this.getInteger("int_value")} catch (_: Exception) {null},
      try {this.getDouble("money_value")} catch (_: Exception) {null},
      try {this.getInteger("multi_select_value")} catch (_: Exception) {null},
    )
  }

  private fun EavWebsiteBool.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteBoolTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value.toInt(),
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value.toInt(),
      )
    }

    return eavWebsiteBoolTuple
  }

  private fun EavWebsiteFloat.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteFloatTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value,
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value,
      )
    }

    return eavWebsiteFloatTuple
  }

  private fun EavWebsiteString.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteStringTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value,
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value,
      )
    }

    return eavWebsiteStringTuple
  }

  private fun EavWebsiteInt.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteIntTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value,
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value,
      )
    }

    return eavWebsiteIntTuple
  }

  private fun EavWebsiteMoney.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteMoneyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value,
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value,
      )
    }

    return eavWebsiteMoneyTuple
  }

  private fun EavWebsiteMultiSelect.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteMultiSelectTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.value,
        this.productId,
        this.websiteId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.websiteId,
        this.attributeKey,
        this.value
      )
    }

    return eavWebsiteMultiSelectTuple
  }

  private fun Eav.toTuple(isPutRequest: Boolean): Tuple {
    val eavWebsiteTuple = if (isPutRequest) {
      Tuple.of(
        this.productId,
        this.attributeKey,
        this.productId,
        this.attributeKey,
      )
    } else {
      Tuple.of(
        this.productId,
        this.attributeKey,
      )
    }

    return eavWebsiteTuple
  }
}
