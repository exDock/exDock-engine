package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.obj
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ProductCompleteEavJdbcVerticle : AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private var list = mutableListOf<Any?>()
  private var currentListName = ""

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    getAllCompleteProductEavData()
    getAllCompleteProductEavDataByProductIdOld()
    getAllCompleteProductEavDataByProductId()
  }

  private fun getAllCompleteProductEavData() {
    val consumer = eventBus.consumer<String>("process.completeEav.getAll")
    consumer.handler { message ->
      val query = "SELECT " +
        "products.product_id AS product_id, " +
        "products.name AS product_name, " +
        "products.short_name AS product_short_name, " +
        "products.description AS product_description, " +
        "products.short_description AS product_short_description, " +
        "e.attribute_key AS attribute_key, " +
        "egb.value AS global_bool, " +
        "egf.value AS global_float, " +
        "egs.value AS global_string, " +
        "egi.value AS global_int, " +
        "egm.value AS global_money, " +
        "egms.value AS global_multi_select, " +
        "esvb.value AS store_view_bool, " +
        "esvf.value AS store_view_float, " +
        "esvs.value AS store_view_string, " +
        "esvi.value AS store_view_int, " +
        "esvm.value AS store_view_money, " +
        "esvms.value AS store_view_multi_select, " +
        "ewb.value AS website_bool, " +
        "ewf.value AS website_float, " +
        "ews.value AS website_string, " +
        "ewi.value AS website_int, " +
        "ewm.value AS website_money, " +
        "ewms.value AS website_multi_select," +
        "msab.value AS multi_select_bool, " +
        "msaf.value AS multi_select_float, " +
        "msas.value AS multi_select_string, " +
        "msai.value AS multi_select_int, " +
        "msam.value AS multi_select_money, " +
        "w.website_id AS website_id, " +
        "sv.store_view_id AS store_view_id " +
        "FROM products " +
        "LEFT JOIN public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.eav_global_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_global_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_global_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_global_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_global_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_global_string egs on products.product_id = egs.product_id " +
        "LEFT JOIN public.eav_website_bool ewb on products.product_id = ewb.product_id " +
        "LEFT JOIN public.eav_website_float ewf on products.product_id = ewf.product_id " +
        "LEFT JOIN public.eav_website_int ewi on products.product_id = ewi.product_id " +
        "LEFT JOIN public.eav_website_money ewm on products.product_id = ewm.product_id " +
        "LEFT JOIN public.eav_website_multi_select ewms on products.product_id = ewms.product_id " +
        "LEFT JOIN public.eav_website_string ews on products.product_id = ews.product_id " +
        "LEFT JOIN public.eav_store_view_bool esvb on products.product_id = esvb.product_id " +
        "LEFT JOIN public.eav_store_view_float esvf on products.product_id = esvf.product_id " +
        "LEFT JOIN public.eav_store_view_int esvi on products.product_id = esvi.product_id " +
        "LEFT JOIN public.eav_store_view_money esvm on products.product_id = esvm.product_id " +
        "LEFT JOIN public.eav_store_view_multi_select esvms on products.product_id = esvms.product_id " +
        "LEFT JOIN public.eav_store_view_string esvs on products.product_id = esvs.product_id " +
        "LEFT JOIN public.multi_select_attributes_bool msab on cpa.attribute_key = msab.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_float msaf on cpa.attribute_key = msaf.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_int msai on cpa.attribute_key = msai.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_string msas on cpa.attribute_key = msas.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_money msam  on cpa.attribute_key = msam.attribute_key " +
        "LEFT JOIN public.store_view sv on esvb.store_view_id = sv.store_view_id " +
        "LEFT JOIN public.websites w on ewb.website_id = w.website_id"
      var json: JsonObject

      val rowsFuture = client.preparedQuery(query).execute()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          json = io.vertx.kotlin.core.json.json {
            obj(
              "completeEav" to rows.map { row ->
                obj(
                  row.makeCompleteEavDataJsonFields()
                )
              }
            )
          }

          message.reply(json)
        } else {
          message.reply("No complete product EAV data found!")
        }
      }
    }
  }

  @Deprecated("Use the newer function below")
  private fun getAllCompleteProductEavDataByProductIdOld() {
    val consumer = eventBus.consumer<Int>("process.completeEav.getByIdOld")
    consumer.handler { message ->
      val query = "SELECT " +
        "products.product_id AS product_id, " +
        "products.name AS product_name, " +
        "products.short_name AS product_short_name, " +
        "products.description AS product_description, " +
        "products.short_description AS product_short_description, " +
        "e.attribute_key AS attribute_key, " +
        "egb.value AS global_bool, " +
        "egf.value AS global_float, " +
        "egs.value AS global_string, " +
        "egi.value AS global_int, " +
        "egm.value AS global_money, " +
        "egms.value AS global_multi_select, " +
        "esvb.value AS store_view_bool, " +
        "esvf.value AS store_view_float, " +
        "esvs.value AS store_view_string, " +
        "esvi.value AS store_view_int, " +
        "esvm.value AS store_view_money, " +
        "esvms.value AS store_view_multi_select, " +
        "ewb.value AS website_bool, " +
        "ewf.value AS website_float, " +
        "ews.value AS website_string, " +
        "ewi.value AS website_int, " +
        "ewm.value AS website_money, " +
        "ewms.value AS website_multi_select," +
        "msab.value AS multi_select_bool, " +
        "msaf.value AS multi_select_float, " +
        "msas.value AS multi_select_string, " +
        "msai.value AS multi_select_int, " +
        "msam.value AS multi_select_money, " +
        "w.website_id AS website_id, " +
        "sv.store_view_id AS store_view_id " +
        "FROM products " +
        "LEFT JOIN public.eav e on products.product_id = e.product_id " +
        "LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key " +
        "LEFT JOIN public.eav_global_bool egb on products.product_id = egb.product_id " +
        "LEFT JOIN public.eav_global_float egf on products.product_id = egf.product_id " +
        "LEFT JOIN public.eav_global_int egi on products.product_id = egi.product_id " +
        "LEFT JOIN public.eav_global_money egm on products.product_id = egm.product_id " +
        "LEFT JOIN public.eav_global_multi_select egms on products.product_id = egms.product_id " +
        "LEFT JOIN public.eav_global_string egs on products.product_id = egs.product_id " +
        "LEFT JOIN public.eav_website_bool ewb on products.product_id = ewb.product_id " +
        "LEFT JOIN public.eav_website_float ewf on products.product_id = ewf.product_id " +
        "LEFT JOIN public.eav_website_int ewi on products.product_id = ewi.product_id " +
        "LEFT JOIN public.eav_website_money ewm on products.product_id = ewm.product_id " +
        "LEFT JOIN public.eav_website_multi_select ewms on products.product_id = ewms.product_id " +
        "LEFT JOIN public.eav_website_string ews on products.product_id = ews.product_id " +
        "LEFT JOIN public.eav_store_view_bool esvb on products.product_id = esvb.product_id " +
        "LEFT JOIN public.eav_store_view_float esvf on products.product_id = esvf.product_id " +
        "LEFT JOIN public.eav_store_view_int esvi on products.product_id = esvi.product_id " +
        "LEFT JOIN public.eav_store_view_money esvm on products.product_id = esvm.product_id " +
        "LEFT JOIN public.eav_store_view_multi_select esvms on products.product_id = esvms.product_id " +
        "LEFT JOIN public.eav_store_view_string esvs on products.product_id = esvs.product_id " +
        "LEFT JOIN public.multi_select_attributes_bool msab on cpa.attribute_key = msab.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_float msaf on cpa.attribute_key = msaf.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_int msai on cpa.attribute_key = msai.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_string msas on cpa.attribute_key = msas.attribute_key " +
        "LEFT JOIN public.multi_select_attributes_money msam  on cpa.attribute_key = msam.attribute_key " +
        "LEFT JOIN public.store_view sv on esvb.store_view_id = sv.store_view_id " +
        "LEFT JOIN public.websites w on ewb.website_id = w.website_id " +
        "WHERE products.product_id =?"
      var json: JsonObject

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(message.body()))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          json = io.vertx.kotlin.core.json.json {
            obj(
              "completeEav" to rows.map { row ->
                obj(
                  row.makeCompleteEavDataJsonFields()
                )
              }
            )
          }

          message.reply(json)
        } else {
          message.reply("No complete product EAV data found!")
        }
      }
    }
  }

  private fun getAllCompleteProductEavDataByProductId() {
    val consumer = eventBus.consumer<Int>("process.completeEav.getById")
    consumer.handler { message ->
      val query = """
            SELECT
            products.product_id AS product_id,
            products.name AS product_name,
            products.short_name AS product_short_name,
            products.description AS product_description,
            products.short_description AS product_short_description,
            products.sku AS product_sku ,
            products.ean AS product_ean ,
            products.manufacturer AS product_manufacturer,
            products.location AS product_location,
            e.attribute_key AS attribute_key,
            egb.value AS global_bool,
            egb.attribute_key AS global_bool_name,
            egf.value AS global_float,
            egf.attribute_key AS global_float_name,
            egs.value AS global_string,
            egs.attribute_key AS global_string_name,
            egi.value AS global_int,
            egi.attribute_key AS global_int_name,
            egm.value AS global_money,
            egm.attribute_key AS global_money_name,
            egms.value AS global_multi_select,
            egms.attribute_key AS global_multi_select_name,
            esvb.value AS store_view_bool,
            esvb.attribute_key AS store_view_bool_name,
            esvf.value AS store_view_float,
            esvf.attribute_key AS store_view_float_name,
            esvs.value AS store_view_string,
            esvs.attribute_key AS store_view_string_name,
            esvi.value AS store_view_int,
            esvi.attribute_key AS store_view_int_name,
            esvm.value AS store_view_money,
            esvm.attribute_key AS store_view_money_name,
            esvms.value AS store_view_multi_select,
            esvms.attribute_key AS store_view_multi_select_name,
            ewb.value AS website_bool,
            ewb.attribute_key AS website_bool_name,
            ewf.value AS website_float,
            ewf.attribute_key AS website_float_name,
            ews.value AS website_string,
            ews.attribute_key AS website_string_name,
            ewi.value AS website_int,
            ewi.attribute_key AS website_int_name,
            ewm.value AS website_money,
            ewm.attribute_key AS website_money_name,
            ewms.value AS website_multi_select,
            ewms.attribute_key AS website_multi_select_name,
            msab.value AS multi_select_bool,
            msab.attribute_key AS multi_select_bool_name,
            msaf.value AS multi_select_float,
            msaf.attribute_key AS multi_select_float_name,
            msas.value AS multi_select_string,
            msas.attribute_key AS multi_select_string_name,
            msai.value AS multi_select_int,
            msai.attribute_key AS multi_select_int_name,
            msam.value AS multi_select_money,
            msam.attribute_key AS multi_select_money_name,
            w.website_id AS website_id,
            sv.store_view_id AS store_view_id,
            pp.price AS product_price,
            pp.cost_price AS product_cost_price,
            pp.sale_price AS product_sale_price,
            pp.tax_class AS product_tax_class,
            pp.sale_date_start AS product_sale_date_start,
            pp.sale_date_end AS product_sale_date_end,
            ps.meta_title AS product_meta_title,
            ps.meta_description AS product_meta_description,
            ps.meta_keywords AS product_meta_keywords,
            ps.page_index AS product_page_index,
            i.image_url,
            i.image_name,
            i.extensions,
            pl.attribute_id AS list_name,
            li.boolean_value,
            li.float_value,
            li.integer_value,
            li.money_value,
            li.multi_select_value,
            li.string_value
            FROM products
            LEFT JOIN public.eav e on products.product_id = e.product_id
            LEFT JOIN public.custom_product_attributes cpa on cpa.attribute_key = e.attribute_key
            LEFT JOIN public.eav_global_bool egb on products.product_id = egb.product_id
            LEFT JOIN public.eav_global_float egf on products.product_id = egf.product_id
            LEFT JOIN public.eav_global_int egi on products.product_id = egi.product_id
            LEFT JOIN public.eav_global_money egm on products.product_id = egm.product_id
            LEFT JOIN public.eav_global_multi_select egms on products.product_id = egms.product_id
            LEFT JOIN public.eav_global_string egs on products.product_id = egs.product_id
            LEFT JOIN public.eav_website_bool ewb on products.product_id = ewb.product_id
            LEFT JOIN public.eav_website_float ewf on products.product_id = ewf.product_id
            LEFT JOIN public.eav_website_int ewi on products.product_id = ewi.product_id
            LEFT JOIN public.eav_website_money ewm on products.product_id = ewm.product_id
            LEFT JOIN public.eav_website_multi_select ewms on products.product_id = ewms.product_id
            LEFT JOIN public.eav_website_string ews on products.product_id = ews.product_id
            LEFT JOIN public.eav_store_view_bool esvb on products.product_id = esvb.product_id
            LEFT JOIN public.eav_store_view_float esvf on products.product_id = esvf.product_id
            LEFT JOIN public.eav_store_view_int esvi on products.product_id = esvi.product_id
            LEFT JOIN public.eav_store_view_money esvm on products.product_id = esvm.product_id
            LEFT JOIN public.eav_store_view_multi_select esvms on products.product_id = esvms.product_id
            LEFT JOIN public.eav_store_view_string esvs on products.product_id = esvs.product_id
            LEFT JOIN public.multi_select_attributes_bool msab on cpa.attribute_key = msab.attribute_key
            LEFT JOIN public.multi_select_attributes_float msaf on cpa.attribute_key = msaf.attribute_key
            LEFT JOIN public.multi_select_attributes_int msai on cpa.attribute_key = msai.attribute_key
            LEFT JOIN public.multi_select_attributes_string msas on cpa.attribute_key = msas.attribute_key
            LEFT JOIN public.multi_select_attributes_money msam  on cpa.attribute_key = msam.attribute_key
            LEFT JOIN public.store_view sv on esvb.store_view_id = sv.store_view_id
            LEFT JOIN public.websites w on ewb.website_id = w.website_id
            LEFT JOIN public.image_product ip ON products.product_id = ip.product_id
            LEFT JOIN public.image i ON ip.image_url = i.image_url and ip.image_name = i.image_name
            LEFT JOIN public.products_pricing pp ON products.product_id = pp.product_id
            LEFT JOIN public.products_seo ps ON products.product_id = ps.product_id
            LEFT JOIN public.product_list pl ON products.product_id = pl.product_id
            LEFT JOIN public.list_items li ON pl.list_item_id = li.list_item_id
            WHERE products.product_id = ?
            """.trimIndent()

      var json: JsonObject? = null

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(message.body()))

      rowsFuture.onFailure {
        it.printStackTrace()
        message.reply("Failed to get Data")
      }

      rowsFuture.onSuccess { rs ->
        val rows = rs.value()
        rows.forEach { row ->
          json = row.makeCompleteEavDataJsonFieldsNew(json)
        }

        message.reply(json)
      }
    }
  }

  private fun Row.makeCompleteEavDataJsonFieldsNew(jsonObject: JsonObject?): JsonObject {
    var newJsonObject = jsonObject

    if (newJsonObject == null) {
      newJsonObject = JsonObject()
      newJsonObject.put("product_id", this.getInteger("product_id"))
      newJsonObject.put("attribute_key", this.getString("attribute_key"))
      newJsonObject.put("store_view_id", this.getInteger("store_view_id"))
      newJsonObject.put("website_id", this.getInteger("website_id"))
      newJsonObject.put("product_name", this.getString("product_name"))
      newJsonObject.put("product_short_name", this.getString("product_short_name"))
      newJsonObject.put("product_description", this.getString("product_description"))
      newJsonObject.put("product_short_description", this.getString("product_short_description"))
      newJsonObject.put("product_sku", this.getString("product_sku"))
      newJsonObject.put("product_ean", this.getString("product_ean"))
      newJsonObject.put("product_manufacturer", this.getString("product_manufacturer"))
      newJsonObject.put("product_price", this.getDouble("product_price"))
      newJsonObject.put("product_cost_price", this.getDouble("product_cost_price"))
      newJsonObject.put("product_sale_price", this.getDouble("product_sale_price"))
      newJsonObject.put("product_tax_class", this.getString("product_tax_class"))
      newJsonObject.put("product_sale_date_start", this.getString("product_sale_date_start"))
      newJsonObject.put("product_sale_date_end", this.getString("product_sale_date_end"))
      newJsonObject.put("product_meta_title", this.getString("product_meta_title"))
      newJsonObject.put("product_meta_description", this.getString("product_meta_description"))
      newJsonObject.put("product_meta_keywords", this.getString("product_meta_keywords"))
      newJsonObject.put("product_page_index", this.getString("product_page_index"))
      newJsonObject.put("product_location", this.getString("product_location"))
    }

    val eavAttributesJson = this.stripStandardValues()
    val iterator = eavAttributesJson.iterator()

    while (iterator.hasNext()) {
      val currentValue: MutableMap.MutableEntry<String, Any>? = iterator.next()
      val value = currentValue?.value
      val key = currentValue?.key
      if (value != null) {
        if (key == "image_url") {
          var currentImages: JsonArray? = null

          try {
            currentImages = newJsonObject.getJsonArray("images")
          } catch (_: Exception) {
          }

          if (currentImages == null) {
            currentImages = JsonArray()
          } else {
            newJsonObject.remove("images")
          }

          val newImage = JsonObject()
          newImage.put("image_url", value)
          newImage.put("image_name", iterator.next().value)
          newImage.put("extensions", iterator.next().value)

          if (!currentImages.contains(newImage)) currentImages.add(newImage)
          newJsonObject.put("images", currentImages)

        } else if (key == "list_name") {
          if (value != currentListName && currentListName != "") {
            newJsonObject.put(key, list)
            list = mutableListOf()
          }
          currentListName = value as String

          for (i in 0 until 6) {
            val listValue = iterator.next().value
            if (listValue != null && !list.contains(listValue)) {
              list.add(listValue)
            }
          }
        } else {
          newJsonObject.put(key, value)
        }
      }
    }

    if (currentListName != "") {
      newJsonObject.put(currentListName, list)
    }

    return newJsonObject
  }

  private fun Row.stripStandardValues(): JsonObject {
    val jsonRow = this.toJson()
    jsonRow.remove("product_id")
    jsonRow.remove("attribute_key")
    jsonRow.remove("store_view_id")
    jsonRow.remove("website_id")
    jsonRow.remove("product_name")
    jsonRow.remove("product_short_name")
    jsonRow.remove("product_description")
    jsonRow.remove("product_short_description")
    jsonRow.remove("product_sku")
    jsonRow.remove("product_ean")
    jsonRow.remove("product_manufacturer")
    jsonRow.remove("product_price")
    jsonRow.remove("product_cost_price")
    jsonRow.remove("product_sale_price")
    jsonRow.remove("product_tax_class")
    jsonRow.remove("product_sale_date_start")
    jsonRow.remove("product_sale_date_end")
    jsonRow.remove("product_meta_title")
    jsonRow.remove("product_meta_description")
    jsonRow.remove("product_meta_keywords")
    jsonRow.remove("product_page_index")
    jsonRow.remove("product_location")

    return jsonRow
  }

  private fun Row.makeCompleteEavDataJsonFields(): List<Pair<String, Any>> {
    return listOf(
      "product_id" to this.getInteger("product_id"),
      "attribute_key" to this.getString("attribute_key"),
      "store_view_id" to this.getInteger("store_view_id"),
      "website_id" to this.getInteger("website_id"),
      "product_name" to this.getString("product_name"),
      "product_short_name" to this.getString("product_short_name"),
      "product_description" to this.getString("product_description"),
      "product_short_description" to this.getString("product_short_description"),
      "global_bool" to this.getBoolean("global_bool"),
      "global_float" to this.getDouble("global_float"),
      "global_string" to this.getString("global_string"),
      "global_int" to this.getInteger("global_int"),
      "global_money" to this.getDouble("global_money"),
      "global_multi_select" to this.getString("global_multi_select"),
      "store_view_bool" to this.getString("store_view_bool"),
      "store_view_float" to this.getDouble("store_view_float"),
      "store_view_string" to this.getString("store_view_string"),
      "store_view_int" to this.getInteger("store_view_int"),
      "store_view_money" to this.getDouble("store_view_money"),
      "store_view_multi_select" to this.getString("store_view_multi_select"),
      "website_bool" to this.getString("website_bool"),
      "website_float" to this.getDouble("website_float"),
      "website_string" to this.getString("website_string"),
      "website_int" to this.getInteger("website_int"),
      "website_money" to this.getDouble("website_money"),
      "website_multi_select" to this.getString("website_multi_select"),
      "multi_select_bool" to this.getString("multi_select_bool"),
      "multi_select_float" to this.getDouble("multi_select_float"),
      "multi_select_string" to this.getString("multi_select_string"),
      "multi_select_int" to this.getInteger("multi_select_int"),
      "multi_select_money" to this.getDouble("multi_select_money")
    )
  }
}
