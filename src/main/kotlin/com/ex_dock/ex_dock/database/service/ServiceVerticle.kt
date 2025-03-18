package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.database.account.Permission
import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import org.mindrot.jbcrypt.BCrypt

class ServiceVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    populateTemplateTable()
    addAdminUser()
    addProductInfoBackendBlock()
  }

  private fun populateTemplateTable() {
    eventBus.consumer<Any?>("process.service.populateTemplates").handler { message ->
      val templateList = getAllStandardTemplates()
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

  private fun addAdminUser() {
    eventBus.consumer<Any?>("process.service.addAdminUser").handler { message ->
      val query = "INSERT INTO users (email, password) SELECT ?,? WHERE" +
        " NOT EXISTS (SELECT * FROM users WHERE email =?)"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
        "test@test.com",
        hashPassword("123456"),
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
      var idBlockId = -1
      var contentBlockId = -1
      var imageBlockId = -1
      var priceBlockId = -1

      var blockFutures: MutableList<Future<RowSet<Row>>> = mutableListOf()
      val query = "INSERT INTO backend_block (block_id, block_name, block_type) VALUES (?,?,?)"
      val rowsFuture1 = client.preparedQuery(query).execute(Tuple.of(null, "Id data", "id_information"))
      blockFutures.add(rowsFuture1)
      rowsFuture1.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture1.onComplete {
        idBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
      }

      val rowsFuture2 = client.preparedQuery(query).execute(Tuple.of(null, "Content", "standard"))
      blockFutures.add(rowsFuture2)
      rowsFuture2.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture2.onComplete {
        contentBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
      }

      val rowsFuture3 = client.preparedQuery(query).execute(Tuple.of(null, "Images", "images"))
      blockFutures.add(rowsFuture3)
      rowsFuture3.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture3.onComplete {
        imageBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
      }

      val rowsFuture4 = client.preparedQuery(query).execute(Tuple.of(null, "Price", "product_price"))
      blockFutures.add(rowsFuture4)
      rowsFuture4.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }
      rowsFuture4.onComplete {
        priceBlockId = it.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
      }

      val attributeQuery = "INSERT INTO block_attributes (attribute_id, attribute_name, attribute_type) SELECT ?,?,? " +
        "WHERE NOT EXISTS (SELECT * FROM block_attributes WHERE attribute_id = ?)"
      val rowsFuture5 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("sku", "SKU", "text", "sku"))
      blockFutures.add(rowsFuture5)
      rowsFuture5.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture6 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("location", "Location", "text", "location"))
      blockFutures.add(rowsFuture6)
      rowsFuture6.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture7 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("ean", "EAN", "text", "ean"))
      blockFutures.add(rowsFuture7)
      rowsFuture7.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture8 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("description", "Description", "wysiwyg", "description"))
      blockFutures.add(rowsFuture8)
      rowsFuture8.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture9 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("short_description", "Short description", "wysiwyg", "short_description"))
      blockFutures.add(rowsFuture9)
      rowsFuture9.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture10 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("cost_price", "Cost price", "price", "cost_price"))
      blockFutures.add(rowsFuture10)
      rowsFuture10.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture11 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("tax_class", "Tax class", "text", "tax_class"))
      blockFutures.add(rowsFuture11)
      rowsFuture11.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture12 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("price", "Price", "price", "price"))
      blockFutures.add(rowsFuture12)
      rowsFuture12.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      val rowsFuture13 = client.preparedQuery(attributeQuery).execute(
        Tuple.of("sale_price", "Sale price", "price", "sale_price"))
      blockFutures.add(rowsFuture13)
      rowsFuture13.onFailure {
        println("Failed to execute query: $it")
        message.fail(500, "Failed to execute query")
      }

      Future.all(blockFutures).onFailure {
        println("Failed to execute all queries: $it")
        message.fail(500, "Failed to execute all queries")
      }.onComplete { _ ->
        blockFutures = mutableListOf()
        println("Completed first step")
        message.reply("")
      }

    }
  }

  private fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt(12))
  }
}
