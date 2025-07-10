package com.ex_dock.ex_dock.database.url

import com.ex_dock.ex_dock.database.category.Categories
import com.ex_dock.ex_dock.database.category.toPageIndex
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.product.Products
import com.ex_dock.ex_dock.database.text_pages.TextPages
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

class UrlJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val urlKeysDeliveryOptions = DeliveryOptions().setCodecName("UrlKeysCodec")
  private val textPageUrlsDeliveryOptions = DeliveryOptions().setCodecName("TextPageUrlsCodec")
  private val categoryUrlsDeliveryOptions = DeliveryOptions().setCodecName("CategoryUrlsCodec")
  private val productUrlsDeliveryOptions = DeliveryOptions().setCodecName("ProductUrlsCodec")
  private val fullUrlsDeliveryOptions = DeliveryOptions().setCodecName("FullUrlKeysCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  companion object {
    private const val CACHE_ADDRESS = "urls"
  }

  override fun start() {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections with the url_keys table
    getAllUrlKeys()
    getUrlByKey()
    createUrlKey()
    updateUrlKey()
    deleteUrlKey()

    // Initialize all eventbus connections with the text_page_urls table
    getAllTextPageUrls()
    getTextPageUrlByKey()
    createTextPageUrl()
    updateTextPageUrl()
    deleteTextPageUrl()

    // Initialize all eventbus connections with the category_urls table
    getAllCategoryUrls()
    getCategoryUrlByKey()
    createCategoryUrl()
    updateCategoryUrl()
    deleteCategoryUrl()

    // Initialize all eventbus connections with the product_urls table
    getAllProductUrls()
    getProductUrlByKey()
    createProductUrl()
    updateProductUrl()
    deleteProductUrl()

    // Initialize all eventbus connections with the full_urls table structure
    getAllFullUrls()
    getFullUrlByKey()
  }

  /**
   * Get all url keys from the database
   */
  private fun getAllUrlKeys() {
    val getAllUrlKeysConsumer = eventBus.consumer<String>("process.url.getAllUrlKeys")
    getAllUrlKeysConsumer.handler { message ->
      val query = "SELECT * FROM url_keys"
      val rowsFuture = client.preparedQuery(query).execute()
      val urlKeyList: MutableList<UrlKeys> = emptyList<UrlKeys>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            urlKeyList.add(row.makeUrlKey())
          }
        }

        message.reply(urlKeyList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a specific url key from the database by url_key and upper_key
   */
  private fun getUrlByKey() {
    val getUrlByKeyConsumer = eventBus.consumer<UrlKeys>("process.url.getUrlByKey")
    getUrlByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM url_keys WHERE url_key =? AND upper_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.urlKey, body.upperKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeUrlKey(), urlKeysDeliveryOptions)
        } else {
          message.reply("No url key found!")
        }
      }
    }
  }

  /**
   * Create a new url key in the database
   */
  private fun createUrlKey() {
    val createUrlKeyConsumer = eventBus.consumer<UrlKeys>("process.url.createUrlKey")
    createUrlKeyConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO url_keys (url_key, upper_key, page_type) VALUES (?,?,?::p_type)"
      val urlKeyTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(urlKeyTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, urlKeysDeliveryOptions)
        } else {
          message.reply("Failed to create url key")
        }
      }
    }
  }

  /**
   * Update an existing url key in the database
   */
  private fun updateUrlKey() {
    val updateUrlKeyConsumer = eventBus.consumer<UrlKeys>("process.url.updateUrlKey")
    updateUrlKeyConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE url_keys SET page_type =?::p_type WHERE url_key =? AND upper_key =?"
      val urlKeyTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(urlKeyTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, urlKeysDeliveryOptions)
        } else {
          println("No url key found to update!")
          message.reply("Failed to update url key")
        }
      }
    }
  }

  /**
   * Delete an existing url key from the database
   */
  private fun deleteUrlKey() {
    val deleteUrlKeyConsumer = eventBus.consumer<UrlKeys>("process.url.deleteUrlKey")
    deleteUrlKeyConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM url_keys WHERE url_key =? AND upper_key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(body.urlKey, body.upperKey))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Url key deleted successfully")
        } else {
          println("No url key found to delete!")
          message.reply("Failed to delete url key")
        }
      }
    }
  }

  /**
   * Get all text page urls from the database
   */
  private fun getAllTextPageUrls() {
    val getAllTextPageUrlsConsumer = eventBus.consumer<String>("process.url.getAllTextPageUrls")
    getAllTextPageUrlsConsumer.handler { message ->
      val query = "SELECT * FROM text_page_urls"
      val rowsFuture = client.preparedQuery(query).execute()
      val textPageUrls: MutableList<TextPageUrls> = emptyList<TextPageUrls>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            textPageUrls.add(row.makeTextPageUrl())
          }
        }

        message.reply(textPageUrls, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a specific text page url from the database by url_key and upper_key
   */
  private fun getTextPageUrlByKey() {
    val getTextPageUrlByKeyConsumer = eventBus.consumer<TextPageUrls>("process.url.getTextPageUrlByKey")
    getTextPageUrlByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM text_page_urls WHERE url_key =? AND upper_key =? AND text_pages_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeTextPageUrl(), textPageUrlsDeliveryOptions)
        } else {
          message.reply("No text page url found!")
        }
      }
    }
  }

  /**
   * Create a new text page url in the database
   */
  private fun createTextPageUrl() {
    val createTextPageUrlConsumer = eventBus.consumer<TextPageUrls>("process.url.createTextPageUrl")
    createTextPageUrlConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO text_page_urls (url_key, upper_key, text_pages_id) VALUES (?,?,?)"
      val textPageUrlTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(textPageUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, textPageUrlsDeliveryOptions)
        } else {
          message.reply("Failed to create text page url")
        }
      }
    }
  }

  /**
   * Update an existing text page url in the database
   */
  private fun updateTextPageUrl() {
    val updateTextPageUrlConsumer = eventBus.consumer<TextPageUrls>("process.url.updateTextPageUrl")
    updateTextPageUrlConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE text_page_urls SET url_key =?, upper_key =?, " +
        "text_pages_id =? WHERE url_key =? AND upper_key =? AND text_pages_id =?"
      val textPageUrlTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(textPageUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, textPageUrlsDeliveryOptions)
        } else {
          message.reply("Failed to update text page url")
        }
      }
    }
  }

  /**
   * Delete an existing text page url from the database
   */
  private fun deleteTextPageUrl() {
    val deleteTextPageUrlConsumer = eventBus.consumer<TextPageUrls>("process.url.deleteTextPageUrl")
    deleteTextPageUrlConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM text_page_urls WHERE url_key =? AND upper_key =? AND text_pages_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Text page url deleted successfully")
        } else {
          message.reply("Failed to delete text page url")
        }
      }
    }
  }

  /**
   * Get all category urls from the database
   */
  private fun getAllCategoryUrls() {
    val getAllCategoryUrlsConsumer = eventBus.consumer<String>("process.url.getAllCategoryUrls")
    getAllCategoryUrlsConsumer.handler { message ->
      val query = "SELECT * FROM category_urls"
      val rowsFuture = client.preparedQuery(query).execute()
      val categoryUrlsList: MutableList<CategoryUrls> = emptyList<CategoryUrls>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach {row ->
            categoryUrlsList.add(row.makeCategoryUrl())
          }
        }

        message.reply(categoryUrlsList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a specific category url from the database by url_key and upper_key
   */
  private fun getCategoryUrlByKey() {
    val getCategoryUrlByKeyConsumer = eventBus.consumer<CategoryUrls>("process.url.getCategoryUrlByKey")
    getCategoryUrlByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM category_urls WHERE url_key =? AND upper_key =? AND category_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeCategoryUrl(), categoryUrlsDeliveryOptions)
        } else {
          message.reply("No category url found!")
        }
      }
    }
  }

  /**
   * Create a new category url in the database
   */
  private fun createCategoryUrl() {
    val createCategoryUrlConsumer = eventBus.consumer<CategoryUrls>("process.url.createCategoryUrl")
    createCategoryUrlConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO category_urls (url_key, upper_key, category_id) VALUES (?,?,?)"
      val categoryUrlTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(categoryUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, categoryUrlsDeliveryOptions)
        } else {
          message.reply("Failed to create category url")
        }
      }
    }
  }

  /**
   * Update an existing category url in the database
   */
  private fun updateCategoryUrl() {
    val updateCategoryUrlConsumer = eventBus.consumer<CategoryUrls>("process.url.updateCategoryUrl")
    updateCategoryUrlConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE category_urls SET url_key =?, upper_key =?, category_id =? " +
        "WHERE url_key =? AND upper_key =? AND category_id =?"
      val categoryUrlTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(categoryUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, categoryUrlsDeliveryOptions)
        } else {
          message.reply("Failed to update category url")
        }
      }
    }
  }

  /**
   * Delete an existing category url from the database
   */
  private fun deleteCategoryUrl() {
    val deleteCategoryUrlConsumer = eventBus.consumer<CategoryUrls>("process.url.deleteCategoryUrl")
    deleteCategoryUrlConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM category_urls WHERE url_key =? AND upper_key =? AND category_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Category url deleted successfully")
        } else {
          message.reply("Failed to delete category url")
        }
      }
    }
  }

  /**
   * Get all product urls from the database
   */
  private fun getAllProductUrls() {
    val getAllProductUrlsConsumer = eventBus.consumer<String>("process.url.getAllProductUrls")
    getAllProductUrlsConsumer.handler { message ->
      val query = "SELECT * FROM product_urls"
      val rowsFuture = client.preparedQuery(query).execute()
      val productUrlsList: MutableList<ProductUrls> = emptyList<ProductUrls>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            productUrlsList.add(row.makeProductUrl())
          }
        }

        message.reply(productUrlsList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a specific product url from the database by url_key and upper_key
   */
  private fun getProductUrlByKey() {
    val getProductUrlByKeyConsumer = eventBus.consumer<ProductUrls>("process.url.getProductUrlByKey")
    getProductUrlByKeyConsumer.handler { message ->
      val body = message.body()
      val query = "SELECT * FROM product_urls WHERE url_key =? AND upper_key =? AND product_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeProductUrl(), productUrlsDeliveryOptions)
        } else {
          message.reply("No product url found!")
        }
      }
    }
  }

  /**
   * Create a new product url in the database
   */
  private fun createProductUrl() {
    val createProductUrlConsumer = eventBus.consumer<ProductUrls>("process.url.createProductUrl")
    createProductUrlConsumer.handler { message ->
      val body = message.body()
      val query = "INSERT INTO product_urls (url_key, upper_key, product_id) VALUES (?,?,?)"
      val productUrlTuple = body.toTuple(false)
      val rowsFuture = client.preparedQuery(query).execute(productUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, productUrlsDeliveryOptions)
        } else {
          message.reply("Failed to create product url")
        }
      }
    }
  }

  /**
   * Update an existing product url in the database
   */
  private fun updateProductUrl() {
    val updateProductUrlConsumer = eventBus.consumer<ProductUrls>("process.url.updateProductUrl")
    updateProductUrlConsumer.handler { message ->
      val body = message.body()
      val query = "UPDATE product_urls SET url_key =?, upper_key =?, product_id =? " +
        "WHERE url_key =? AND upper_key =? AND product_id =?"
      val productUrlTuple = body.toTuple(true)
      val rowsFuture = client.preparedQuery(query).execute(productUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply(body, productUrlsDeliveryOptions)
        } else {
          message.reply("Failed to update product url")
        }
      }
    }
  }

  /**
   * Delete an existing product url from the database
   */
  private fun deleteProductUrl() {
    val deleteProductUrlConsumer = eventBus.consumer<ProductUrls>("process.url.deleteProductUrl")
    deleteProductUrlConsumer.handler { message ->
      val body = message.body()
      val query = "DELETE FROM product_urls WHERE url_key =? AND upper_key =? AND product_id =?"
      val rowsFuture = client.preparedQuery(query).execute(body.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
          message.reply("Product url deleted successfully")
        } else {
          message.reply("Failed to delete product url")
        }
      }
    }
  }

  /**
   * Get all full urls from the database
   */
  private fun getAllFullUrls() {
    val getAllFullUrlsConsumer = eventBus.consumer<FullUrlRequestInfo>("process.url.getAllFullUrls")
    getAllFullUrlsConsumer.handler { message ->
      val body = message.body()
      val joinList = body.joinList.checkJoinMessage()
      val query = makeFullUrlKeyQuery(joinList, false)

      val rowsFuture = client.preparedQuery(query).execute()
      val fullUrlKeysList: MutableList<FullUrlKeys> = emptyList<FullUrlKeys>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            fullUrlKeysList.add(row.makeFullUrlKey(joinList))
          }
        }

        message.reply(fullUrlKeysList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a specific full url from the database by url_key and upper_key
   */
  private fun getFullUrlByKey() {
    val getFullUrlByKeyConsumer = eventBus.consumer<FullUrlRequestInfo>("process.url.getFullUrlByKey")
    getFullUrlByKeyConsumer.handler { message ->
      val body = message.body()
      val joinList = body.joinList.checkJoinMessage()
      val query = makeFullUrlKeyQuery(joinList, true)

      val rowsFuture = client.preparedQuery(query).execute(
        Tuple.of(
          body.urlKeys,
          body.upperKey
        )
      )

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeFullUrlKey(joinList), fullUrlsDeliveryOptions)
        } else {
          message.reply("No full url found!")
        }
      }
    }
  }

  /**
   * Create JSON fields for the url keys
   *
   * @param row The row from the database
   * @return A list with the converted fields from the database
   */
  private fun Row.makeUrlKey(): UrlKeys {
    return  UrlKeys(
      urlKey = this.getString("url_key"),
      upperKey = this.getString("upper_key"),
      pageType = this.getString("page_type").toPageType()
    )
  }

  /**
   * Create JSON fields for the text page urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the database
   */
  private fun Row.makeTextPageUrl(): TextPageUrls {
    return TextPageUrls(
      urlKeys = this.getString("url_key"),
      upperKey = this.getString("upper_key"),
      textPagesId = this.getInteger("text_pages_id")
    )
  }

  /**
   * Create JSON fields for the category urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the database
   */
  private fun Row.makeCategoryUrl(): CategoryUrls {
    return CategoryUrls(
      urlKeys = this.getString("url_key"),
      upperKey = this.getString("upper_key"),
      categoryId = this.getInteger("category_id"),
    )
  }

  /**
   * Create JSON fields for the product urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the databas
   */
  private fun Row.makeProductUrl(): ProductUrls {
    return ProductUrls(
      urlKeys = this.getString("url_key"),
      upperKey = this.getString("upper_key"),
      productId = this.getInteger("product_id"),
    )
  }

  /**
   * Create JSON fields for the full url key query
   *
   * @param row The row from the database
   * @param joinList A list of booleans indicating whether to join with text_pages, categories, or products
   * @return The constructed query string
   */
  private fun Row.makeFullUrlKey(joinList: List<Boolean>): FullUrlKeys {
    val fullUrlKeys = FullUrlKeys(
      urlKeys = this.makeUrlKey(),
      textPage = null,
      category = null,
      product = null
    )

    if (joinList[0]) {
      val textPages = TextPages(
        textPagesId = this.getInteger("text_pages_id"),
        name = this.getString("text_page_name"),
        shortText = this.getString("text_page_short_text"),
        text = this.getString("text_page_text")
      )

      fullUrlKeys.textPage = textPages
    }

    if (joinList[1]) {
      val category = Categories(
        categoryId = this.getInteger("category_id"),
        upperCategory = this.getInteger("upper_category"),
        name = this.getString("category_name"),
        description = this.getString("category_description"),
        shortDescription = this.getString("category_short_description")
      )

      fullUrlKeys.category = category
    }

    if (joinList[2]) {
      val product = Products(
        productId = this.getInteger("product_id"),
        name = this.getString("product_name"),
        shortName = this.getString("product_short_name"),
        description = this.getString("product_description"),
        shortDescription = this.getString("product_short_description"),
        sku = this.getString("product_sku"),
        ean = this.getString("product_ean"),
        manufacturer = this.getString("product_manufacturer"),
      )

      fullUrlKeys.product = product
    }

    return fullUrlKeys
  }

  /**
   * Makes a Tuple for the url key query
   *
   * @param body The body to convert into a Tuple
   * @param isPutRequest if the request is a put request
   * @return The Tuple with the data from the body
   */
  private fun UrlKeys.toTuple(isPutRequest: Boolean): Tuple {
    val urlKeyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.pageType.convertToString(),
        this.urlKey,
        this.upperKey,
      )
    } else {
      Tuple.of(
        this.urlKey,
        this.upperKey,
        this.pageType.convertToString(),
      )
    }

    return urlKeyTuple
  }

  /**
   * Makes a Tuple for the text page url
   *
   * @param body The body to convert into a Tuple
   * @param isPutRequest if the request is a put request
   * @return The Tuple with the data from the body
   */
  private fun TextPageUrls.toTuple(isPutRequest: Boolean): Tuple {
    val textPageUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.textPagesId,
        this.urlKeys,
        this.upperKey,
        this.textPagesId,
      )
    } else {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.textPagesId,
      )
    }

    return textPageUrlTuple
  }

  /**
   * Makes a Tuple for the category url
   *
   * @param body The body to convert into a Tuple
   * @param isPutRequest if the request is a put request
   * @return The Tuple with the data from the body
   */
  private fun CategoryUrls.toTuple(isPutRequest: Boolean): Tuple {
    val categoryUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.categoryId,
        this.urlKeys,
        this.upperKey,
        this.categoryId,
      )
    } else {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.categoryId,
      )
    }

    return categoryUrlTuple
  }

  /**
   * Makes a Tuple for the product url
   *
   * @param body The body to convert into a Tuple
   * @param isPutRequest if the request is a put request
   * @return The Tuple with the data from the body
   */
  private fun ProductUrls.toTuple(isPutRequest: Boolean): Tuple {
    val productUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.productId,
        this.urlKeys,
        this.upperKey,
        this.productId,
      )
    } else {
      Tuple.of(
        this.urlKeys,
        this.upperKey,
        this.productId,
      )
    }

    return productUrlTuple
  }

  /**
   * Checks which tables to join from a JsonObject
   *
   * @param body The body which has the information of the joins
   * @return A list with booleans of the tables to join
   */
  private fun JoinList.checkJoinMessage(): MutableList<Boolean> {
    val joinList: MutableList<Boolean> = mutableListOf(false, false, false)

    joinList[0]= try {
      this.joinTextPage
    } catch (e: NullPointerException) { false}
    joinList[1] = try {
      this.joinCategory
    } catch (e: NullPointerException) { false }
    joinList[2] = try {
      this.joinProduct
    } catch (e: NullPointerException) { false }

    return joinList
  }

  /**
   * The query constructor for full url key search
   *
   * @param joinList The list of booleans of the tables to join
   * @return A query string to run on the query
   */
  private fun makeFullUrlKeyQuery(joinList: List<Boolean>, isByKey: Boolean): String {
    var query = renameFullUrlKeyColumns(joinList)

    if (joinList[0]) {
      query += " INNER JOIN text_page_urls tpu ON uk.url_key = tpu.url_key AND uk.upper_key = " +
        "tpu.upper_key " +
        "INNER JOIN public.text_pages tp on tp.text_pages_id = tpu.text_pages_id"
    }
    if (joinList[1]) {
      query += " INNER JOIN category_urls cu ON uk.url_key = cu.url_key AND uk.upper_key = " +
        "cu.upper_key INNER JOIN public.categories c on c.category_id = cu.category_id"
    }
    if (joinList[2]) {
      query += " INNER JOIN public.product_urls pu ON uk.url_key = " +
        "pu.url_key AND uk.upper_key = pu.upper_key " +
        "INNER JOIN public.products p ON p.product_id = pu.product_id"
    }

    if (isByKey) {
      query += " WHERE uk.url_key =? AND uk.upper_key =?"
    }

    return query
  }

  /**
   * A query constructor to rename all the query parameters so
   * that they don't conflict with different tables
   *
   * @param joinList the list of booleans of which tables to join
   * @return A query with changed parameters
   */
  private fun renameFullUrlKeyColumns(joinList: List<Boolean>): String {
    var columnNamesQuery = "SELECT uk.url_key, uk.upper_key, uk.page_type"

    if (joinList[0]) {
      columnNamesQuery += ", tp.text_pages_id, tp.name AS text_page_name, " +
        "tp.short_text AS text_page_short_text, tp.text AS text_page_text"
    }

    if (joinList[1]) {
      columnNamesQuery += ", c.category_id, c.upper_category, c.name AS category_name, " +
        "c.short_description AS category_short_description, c.description AS category_description"
    }

    if (joinList[2]) {
      columnNamesQuery += ", p.product_id, p.name AS product_name, p.short_name AS product_short_name, " +
        "p.description AS product_description, p.short_description AS product_short_description " +
        "p.sku AS product_sku, p.ean AS product_ean, p.manufacturer AS product_manufacturer"
    }

    columnNamesQuery += " FROM url_keys uk "
    return columnNamesQuery
  }
}
