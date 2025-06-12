package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.database.account.Permission
import com.ex_dock.ex_dock.database.account.hash
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.helper.convertImage
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class ServiceVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    populateTemplateTable()
    addAdminUser()
    addProductInfoBackendBlock()
    addTestProduct()
    imageConverter()
  }

  private fun populateTemplateTable() {
    eventBus.consumer<Any?>("process.service.populateTemplates").handler { message ->
      val templateList = getAllStandardTemplatesData()
      val query = "INSERT INTO templates (template_key, template_data, data_string) SELECT ?, ?, ? " +
        "WHERE NOT EXISTS(SELECT * FROM templates WHERE template_key = ?)"
      for (template in templateList) {
        val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
          template.templateKey,
          template.templateData,
          template.dataString,
          template.templateKey
        ))

        rowsFuture.onFailure { res ->
          println("Failed to execute query: $res")
          message.fail(500, "Failed to execute query")
        }
      }

      message.reply("Completed populating templates")
    }
  }

  private fun addTestProduct() {
    eventBus.consumer<Any?>("process.service.addTestProduct").handler { message ->
      val query = "INSERT INTO products (name, short_name, description, short_description, sku, ean, manufacturer) " +
        "SELECT ?,?,?,?,?,?,? WHERE NOT EXISTS (SELECT * FROM products WHERE product_id = ?)"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
        "testProduct",
        "testProduct",
        "This is a test product",
        "A test product for testing purposes",
        "test-sku",
        "test-ean",
        "test-manufacturer",
        1
      ))
      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture.onSuccess { res ->
        var productId = 1
        try {
          productId = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        } catch (_: Exception) {}
        val productSeoQuery = "INSERT INTO products_seo (product_id, meta_title, meta_description, meta_keywords, page_index) " +
          "SELECT ?,?,?,?,?::p_index WHERE NOT EXISTS (SELECT * FROM products_seo WHERE product_id = ?)"
        val rowsFuture2 = client.preparedQuery(productSeoQuery).execute(Tuple.of(
          productId,
          "Test Product - Meta Title",
          "Test Product - Meta Description",
          "Test Product - Meta Keywords",
          "index, follow",
          productId
        ))
        rowsFuture2.onFailure { res ->
          println("Failed to execute query: $res")
          message.fail(500, "Failed to execute query")
        }

        val productsPricingQuery = "INSERT INTO products_pricing (product_id, price, sale_price, cost_price, tax_class) SELECT " +
          "?,?,?,?,? WHERE NOT EXISTS (SELECT * FROM products_pricing WHERE product_id = ?)"
        val rowsFuture3 = client.preparedQuery(productsPricingQuery).execute(Tuple.of(
          productId,
          10.0,
          8.0,
          5.0,
          "tax_class_1",
          productId
        ))
        rowsFuture3.onFailure { res ->
          println("Failed to execute query: $res")
          message.fail(500, "Failed to execute query")
        }

        val imagesQuery = "INSERT INTO image (image_url, image_name, extensions) SELECT ?,?,? " +
          "WHERE NOT EXISTS (SELECT * FROM image WHERE image_url = ?)"
        val rowsFuture4 = client.preparedQuery(imagesQuery).execute(Tuple.of(
          "https://picsum.photos/200/1000",
          "test-image",
          "jpg,png,webp",
          "https://picsum.photos/200/1000"
        ))
        rowsFuture4.onFailure { res ->
          println("Failed to execute query: $res")
          message.fail(500, "Failed to execute query")
        }

        val productImagesQuery = "INSERT INTO image_product (product_id, image_url) SELECT ?,? " +
          "WHERE NOT EXISTS (SELECT * FROM image_product WHERE image_url = ?)"
        val rowsFuture5 = client.preparedQuery(productImagesQuery).execute(Tuple.of(
          productId,
          "https://picsum.photos/200/1000",
          "https://picsum.photos/200/1000"
        ))
        rowsFuture5.onFailure { res ->
          println("Failed to execute query: $res")
          message.fail(500, "Failed to execute query")
        }

        message.reply("Successful")
      }
    }
  }

  private fun addAdminUser() {
    eventBus.consumer<Any?>("process.service.addAdminUser").handler { message ->
      val query = "INSERT INTO users (email, password) SELECT ?,? WHERE" +
        " NOT EXISTS (SELECT * FROM users WHERE email =?)"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
        "test@test.com",
        "123456".hash(),
        "test@test.com"
      ))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(500, "Failed to execute query")
      }

      rowsFuture.onSuccess { res ->
        if (res.property(JDBCPool.GENERATED_KEYS) == null) {
          message.reply("Admin already exists!")
          return@onSuccess
        }
        val userId = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        println(Permission.toString(Permission.READ_WRITE))
        val permissionQuery = "INSERT INTO backend_permissions " +
          "(user_id, user_permissions, server_settings, template, category_content, category_products, " +
          "product_content, product_price, product_warehouse, text_pages, \"API_KEY\") VALUES " +
          "(?,?::b_permissions,?::b_permissions,?::b_permissions,?::b_permissions,?::b_permissions,?::b_permissions," +
          "?::b_permissions,?::b_permissions,?::b_permissions,?)"
        val permissionTuple = Tuple.of(
          userId,
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          Permission.toString(Permission.READ_WRITE),
          null
        )

        client.preparedQuery(permissionQuery).execute(permissionTuple).onFailure { failure ->
          println("Failed to execute query: $failure")
          message.fail(500, "Failed to execute query")
        }.onSuccess {
          message.reply("Completed adding admin user")
        }
      }
    }
  }

  private fun addProductInfoBackendBlock() {
    // TODO: Remove this when the backend can add the correct pages itself
    eventBus.consumer<Any?>("process.service.addProductInfoBackendBlock").handler { message ->
      var idBlockId: Int? = -1
      var contentBlockId: Int? = -1
      var imageBlockId: Int? = -1
      var priceBlockId: Int? = -1

      var blockFutures: MutableList<Future<RowSet<Row>>> = mutableListOf()
      val query = "INSERT INTO backend_block (block_name, block_type) SELECT ?,? WHERE " +
        "NOT EXISTS (SELECT * FROM backend_block WHERE block_name = ?)"
      val rowsFuture1 = client.preparedQuery(query).execute(Tuple.of( "Id data", "id_information", "Id data"))
      blockFutures.add(rowsFuture1)
      rowsFuture1.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture1.onComplete {
        if (it.result().property(JDBCPool.GENERATED_KEYS) == null) {
          message.reply("")
          return@onComplete
        } else {
          idBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        }
      }

      val rowsFuture2 = client.preparedQuery(query).execute(Tuple.of("Content", "standard", "Content"))
      blockFutures.add(rowsFuture2)
      rowsFuture2.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture2.onComplete {
        if (it.result().property(JDBCPool.GENERATED_KEYS) == null) {
          message.reply("")
          return@onComplete
        } else {
          contentBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        }
      }

//      val rowsFuture3 = client.preparedQuery(query).execute(Tuple.of("Images", "images"))
//      blockFutures.add(rowsFuture3)
//      rowsFuture3.onFailure {
//        println("Failed to execute query: $it")
//        message.fail(500, "Failed to execute query")
//      }
//      rowsFuture3.onComplete {
//        imageBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
//      }

      val rowsFuture4 = client.preparedQuery(query).execute(Tuple.of("Price", "product_price", "Price"))
      blockFutures.add(rowsFuture4)
      rowsFuture4.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture4.onComplete {
        if (it.result().property(JDBCPool.GENERATED_KEYS) == null) {
          message.reply("")
          return@onComplete
        } else {
          priceBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        }
      }

      val attributeQuery = "INSERT INTO block_attributes (attribute_id, attribute_name, attribute_type) SELECT ?,?,? " +
        "WHERE NOT EXISTS (SELECT * FROM block_attributes WHERE attribute_id = ?)"
      val rowsFuture5 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_sku", "SKU", "text", "product_sku"))
      blockFutures.add(rowsFuture5)
      rowsFuture5.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture6 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_location", "Location", "text", "product_location"))
      blockFutures.add(rowsFuture6)
      rowsFuture6.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture7 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_ean", "EAN", "text", "product_ean"))
      blockFutures.add(rowsFuture7)
      rowsFuture7.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture18 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_manufacturer", "Manufacturer", "text", "product_manufacturer"))
      blockFutures.add(rowsFuture18)
      rowsFuture18.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture8 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_description", "Description", "wysiwyg", "product_description"))
      blockFutures.add(rowsFuture8)
      rowsFuture8.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture9 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_short_description", "Short description", "wysiwyg", "product_short_description"))
      blockFutures.add(rowsFuture9)
      rowsFuture9.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture10 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_cost_price", "Cost price", "price", "product_cost_price"))
      blockFutures.add(rowsFuture10)
      rowsFuture10.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture11 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_tax_class", "Tax class", "text", "product_tax_class"))
      blockFutures.add(rowsFuture11)
      rowsFuture11.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture12 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_price", "Price", "price", "product_price"))
      blockFutures.add(rowsFuture12)
      rowsFuture12.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture13 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_sale_price", "Sale price", "price", "product_sale_price"))
      blockFutures.add(rowsFuture13)
      rowsFuture13.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture26 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("product_sale_dates", "Sale dates", "list", "product_sale_dates"))
      blockFutures.add(rowsFuture26)
      rowsFuture26.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      Future.all(blockFutures).onFailure {
        println("Failed to execute all queries: $it")
        message.fail(500, "Failed to execute all queries")
      }.onComplete { _ ->
        if (idBlockId == null || contentBlockId == null || imageBlockId == null || priceBlockId == null) message.reply("")
        blockFutures = mutableListOf()
        idBlockId = 4
        contentBlockId = 5
        imageBlockId = 3
        priceBlockId = 6
        val addAttributeBlockQuery = "INSERT INTO attribute_block (block_id, attribute_id) SELECT ?,? " +
          "WHERE NOT EXISTS (SELECT * FROM attribute_block WHERE block_id = ? AND attribute_id = ?)"
        val rowsFuture14 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(idBlockId, "product_sku", idBlockId, "product_sku"))
        blockFutures.add(rowsFuture14)
        rowsFuture14.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture15 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(idBlockId, "product_location", idBlockId, "product_location"))
        blockFutures.add(rowsFuture15)
        rowsFuture15.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture16 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(idBlockId, "product_ean", idBlockId, "product_ean"))
        blockFutures.add(rowsFuture16)
        rowsFuture16.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture17 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(idBlockId, "product_manufacturer", idBlockId, "product_manufacturer"))
        blockFutures.add(rowsFuture17)
        rowsFuture17.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture19 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(contentBlockId, "product_description", contentBlockId, "product_description"))
        blockFutures.add(rowsFuture19)
        rowsFuture19.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture20 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(contentBlockId, "product_short_description", contentBlockId, "product_short_description"))
        blockFutures.add(rowsFuture20)
        rowsFuture20.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture22 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(priceBlockId, "product_price", priceBlockId, "product_price"))
        blockFutures.add(rowsFuture22)
        rowsFuture22.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

        val rowsFuture23 = client.preparedQuery(addAttributeBlockQuery).execute(
          Tuple.of(priceBlockId, "product_sale_price", priceBlockId, "product_sale_price"))
        blockFutures.add(rowsFuture23)
        rowsFuture23.onFailure {
          println("Failed to execute query: $it")
          message.fail(500, "Failed to execute query")
        }

//        val rowsFuture25 = client.preparedQuery(addAttributeBlockQuery).execute(
//          Tuple.of(priceBlockId, "sale_dates", priceBlockId, "sale_dates"))
//        blockFutures.add(rowsFuture25)
//        rowsFuture25.onFailure {
//          println("Failed to execute query: $it")
//          message.fail(500, "Failed to execute query")
//        }

//        val addMultiSelectQuery = "INSERT INTO eav_attribute_multi_select (attribute_id, attribute_key, value) SELECT " +
//          "?,?,? WHERE NOT EXISTS (SELECT * FROM eav_attribute_multi_select WHERE attribute_id = ? AND attribute_key = ? " +
//          "AND value = ?)"
//        val rowsFuture24 = client.preparedQuery(addMultiSelectQuery).execute(Tuple.of(
//          "sale_dates", "sale_date_start", "", idBlockId, "tax_class", "tax_class", "tax_class"))


        Future.all(blockFutures).onFailure {
          println("Failed to execute all queries: $it")
          message.fail(500, "Failed to execute all queries")
        }.onComplete {
          message.reply("")
        }
      }

    }
  }

  private fun imageConverter() {
    eventBus.consumer("process.service.convertImage") { message ->
      val path = message.body()
      println("Got request")
      convertImage(path)
      message.reply("Image conversion completed")
    }
  }
}
