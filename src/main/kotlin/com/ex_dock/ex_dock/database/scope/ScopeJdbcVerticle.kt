package com.ex_dock.ex_dock.database.scope

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class ScopeJdbcVerticle:  AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val websiteDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("WebsitesCodec")
  private val storeViewDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("StoreViewCodec")
  private val fullScopeDeliveryOptions: DeliveryOptions = DeliveryOptions().setCodecName("FullScopeCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  companion object {
    private const val CACHE_ADDRESS = "scopes"
  }

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for the website table
    getAllWebsites()
    getWebsiteById()
    createWebsite()
    editWebsite()
    deleteWebsite()

    // Initialize all eventbus connections for the Store View table
    getAllStoreViews()
    getStoreViewById()
    createStoreView()
    editStoreView()
    deleteStoreView()

    // Initialize all eventbus connections for the Full Scope tables
    getAllScopes()
    getScopeById()
  }

  /**
   * Get all websites from the database
   */
  private fun getAllWebsites() {
    val getAllWebsitesConsumer = eventBus.consumer<Unit>("process.scope.getAllWebsites")
    getAllWebsitesConsumer.handler { message ->
      val query = "SELECT * FROM websites"
      val rowsFuture = client.preparedQuery(query).execute()
      val websites: MutableList<Websites> = emptyList<Websites>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            rows.forEach { row ->
              websites.add(row.makeWebsite())
            }
          }

          message.reply(websites, listDeliveryOptions)
        }
      }
    }
  }

  /**
   * Get a website by its ID from the database
   */
  private fun getWebsiteById() {
    val getWebsiteByIdConsumer = eventBus.consumer<Int>("process.scope.getWebsiteById")
    getWebsiteByIdConsumer.handler { message ->
      val websiteId = message.body()
      val query = "SELECT * FROM websites WHERE website_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(websiteId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            message.reply(rows.first().makeWebsite(), websiteDeliveryOptions)
          } else {
            message.reply("No website found!")
          }
        }
      }
    }
  }

  /**
   * Create a new website in the database
   */
  private fun createWebsite() {
    val createWebsiteConsumer = eventBus.consumer<Websites>("process.scope.createWebsite")
    createWebsiteConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO websites (website_name) VALUES (?)"
      val websiteTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(websiteTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          body.websiteId = res.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, websiteDeliveryOptions)
        } else {
          message.reply("Failed to create website")
        }
      }
    }
  }

  /**
   * Edit an existing website in the database
   */
  private fun editWebsite() {
    val editWebsiteConsumer = eventBus.consumer<Websites>("process.scope.editWebsite")
    editWebsiteConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE websites SET website_name =? WHERE website_id =?"
      val websiteTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(websiteTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, websiteDeliveryOptions)
        } else {
          message.reply("Failed to update website")
        }
      }
    }
  }

  /**
   * Delete a website from the database
   */
  private fun deleteWebsite() {
    val deleteWebsiteConsumer = eventBus.consumer<Int>("process.scope.deleteWebsite")
    deleteWebsiteConsumer.handler { message ->
      val websiteId = message.body()
      val query = "DELETE FROM websites WHERE website_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(websiteId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Website deleted successfully")
        } else {
          message.reply("Failed to delete website")
        }
      }
    }
  }

  /**
   * Get all store views from the database
   */
  private fun getAllStoreViews() {
    val getAllStoreViewsConsumer = eventBus.consumer<Unit>("process.scope.getAllStoreViews")
    getAllStoreViewsConsumer.handler { message ->
      val query = "SELECT * FROM store_view"
      val rowsFuture = client.preparedQuery(query).execute()
      val storeViews: MutableList<StoreView> = emptyList<StoreView>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            rows.forEach { row ->
              storeViews.add(row.makeStoreView())
            }
          }

          message.reply(storeViews, listDeliveryOptions)
        }
      }
    }
  }

  /**
   * Get a store view by its ID from the database
   */
  private fun getStoreViewById() {
    val getStoreViewByIdConsumer = eventBus.consumer<Int>("process.scope.getStoreViewById")
    getStoreViewByIdConsumer.handler { message ->
      val storeViewId = message.body()
      val query = "SELECT * FROM store_view WHERE store_view_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(storeViewId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            message.reply(rows.first().makeStoreView(), storeViewDeliveryOptions)
          } else {
            message.reply("No store view found!")
          }
        }
      }
    }
  }

  /**
   * Create a new store view in the database
   */
  private fun createStoreView() {
    val createStoreViewConsumer = eventBus.consumer<StoreView>("process.scope.createStoreView")
    createStoreViewConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO store_view (website_id, store_view_name) VALUES (?,?)"
      val storeViewTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(storeViewTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          body.storeViewId = res.result().property(JDBCPool.GENERATED_KEYS).getInteger(0)
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, storeViewDeliveryOptions)
        } else {
          message.reply("Failed to create store view")
        }
      }
    }
  }

  /**
   * Edit an existing store view in the database
   */
  private fun editStoreView() {
    val editStoreViewConsumer = eventBus.consumer<StoreView>("process.scope.editStoreView")
    editStoreViewConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE store_view SET website_id =?, store_view_name =? WHERE store_view_id =?"
      val storeViewTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(storeViewTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, storeViewDeliveryOptions)
        } else {
          message.reply("Failed to update store view")
        }
      }
    }
  }

  /**
   * Delete a store view from the database
   */
  private fun deleteStoreView() {
    val deleteStoreViewConsumer = eventBus.consumer<Int>("process.scope.deleteStoreView")
    deleteStoreViewConsumer.handler { message ->
      val storeViewId = message.body()
      val query = "DELETE FROM store_view WHERE store_view_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(storeViewId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Store view deleted successfully")
        } else {
          message.reply("Failed to delete store view")
        }
      }
    }
  }

  /**
   * Get all scopes (website and store view) from the database
   */
  private fun getAllScopes() {
    val getAllScopesConsumer = eventBus.consumer<Unit>("process.scope.getAllScopes")
    getAllScopesConsumer.handler { message ->
      val query = "SELECT * FROM store_view INNER JOIN websites ON store_view.website_id = websites.website_id"
      val rowsFuture = client.preparedQuery(query).execute()
      val fullScopes: MutableList<FullScope> = emptyList<FullScope>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            rows.forEach { row ->
              fullScopes.add(row.makeFullScope())
            }
          }

          message.reply(fullScopes, listDeliveryOptions)
        }
      }
    }
  }

  /**
   * Get a scope by its ID from the database
   */
  private fun getScopeById() {
    val getScopeByIdConsumer = eventBus.consumer<Int>("process.scope.getScopeById")
    getScopeByIdConsumer.handler { message ->
      val scopeId = message.body()
      val query = "SELECT * FROM store_view" +
        " INNER JOIN websites ON store_view.website_id = websites.website_id" +
        " WHERE store_view_id =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(scopeId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          val rows = res.result()
          if (rows.size() > 0) {
            message.reply(rows.first().makeFullScope(), fullScopeDeliveryOptions)
          } else {
            message.reply("No full scope found!")
          }
        }
      }
    }
  }

  /**
   * Make the JSON fields for the return of the website queries
   *
   * @param row The row from the database
   * @return A list with the JSON fields in the format of a Pair object
   */
  private fun Row.makeWebsite(): Websites {
    return Websites(
      websiteId = this.getInteger("website_id"),
      websiteName = this.getString("website_name")
    )
  }

  /**
   * Make the JSON fields for the return of the store view queries
   *
   * @param row The row from the database
   * @return A list with the JSON fields in the format of a Pair object
   **/
  private fun Row.makeStoreView(): StoreView {
    return StoreView(
      storeViewId = this.getInteger("store_view_id"),
      storeViewName = this.getString("store_view_name"),
      websiteId = this.getInteger("website_id")
    )
  }

  /**
   * Make the JSON fields for the return of the full scope queries
   *
   * @param row The row from the database
   * @return A list with the JSON fields in the format of a Pair object
   **/
  private fun Row.makeFullScope(): FullScope {
    return FullScope(
      this.makeWebsite(),
      this.makeStoreView()
    )
  }

  /**
   * Make the tuple for the website queries
   *
   * @param body The JSON object containing the request body
   * @param putRequest Whether it's a PUT request or not
   *
   * @return A Tuple with the query parameters
   **/
  private fun Websites.toTuple(putRequest: Boolean): Tuple {

    val websiteTuple: Tuple = if (putRequest) {
      Tuple.of(
        this.websiteName,
        this.websiteId
      )
    } else {
      Tuple.of(
        this.websiteName
      )
    }

    return websiteTuple
  }

  /**
   * Make the tuple for the store view queries
   *
   * @param body The JSON object containing the request body
   * @param putRequest Whether it's a PUT request or not
   *
   * @return A Tuple with the query parameters
   **/
  private fun StoreView.toTuple(putRequest: Boolean): Tuple {
    val storeViewTuple: Tuple = if (putRequest) {
      Tuple.of(
        this.websiteId,
        this.storeViewName,
        this.storeViewId
      )
    } else {
      Tuple.of(
        this.websiteId,
        this.storeViewName,
      )
    }

    return storeViewTuple
  }
}
