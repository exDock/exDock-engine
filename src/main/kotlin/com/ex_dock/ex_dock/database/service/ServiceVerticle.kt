package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.database.account.Permission
import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
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
  }

  private fun populateTemplateTable() {
    eventBus.consumer<Any?>("process.service.populateTemplates").handler { message ->
      val templateList = getAllStandardTemplates()
      val query = "INSERT INTO templates (template_key, template_data) SELECT ?, ? " +
        "WHERE NOT EXISTS(SELECT * FROM templates WHERE template_key = ?)"
      for (template in templateList) {
        val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
          template.templateKey,
          template.templateData,
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

  private fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt(12))
  }
}
