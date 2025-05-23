package com.ex_dock.ex_dock.database.url

import com.ex_dock.ex_dock.database.category.Categories
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.product.Products
import com.ex_dock.ex_dock.database.text_pages.TextPages
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

  override fun start() {
    client = getConnection(vertx)
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
            urlKeyList.add(makeUrlKey(row))
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
          message.reply(makeUrlKey(rows.first()), urlKeysDeliveryOptions)
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
      val urlKeyTuple = makeUrlKeyTuple(body, false)
      val rowsFuture = client.preparedQuery(query).execute(urlKeyTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
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
      val urlKeyTuple = makeUrlKeyTuple(body, true)
      val rowsFuture = client.preparedQuery(query).execute(urlKeyTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.result().rowCount() > 0) {
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
            textPageUrls.add(makeTextPageUrl(row))
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
      val rowsFuture = client.preparedQuery(query).execute(makeTextPageUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeTextPageUrl(rows.first()), textPageUrlsDeliveryOptions)
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
      val textPageUrlTuple = makeTextPageUrlTuple(body, false)
      val rowsFuture = client.preparedQuery(query).execute(textPageUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val textPageUrlTuple = makeTextPageUrlTuple(body, true)
      val rowsFuture = client.preparedQuery(query).execute(textPageUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val rowsFuture = client.preparedQuery(query).execute(makeTextPageUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
            categoryUrlsList.add(makeCategoryUrl(row))
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
      val rowsFuture = client.preparedQuery(query).execute(makeCategoryUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeCategoryUrl(rows.first()), categoryUrlsDeliveryOptions)
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
      val categoryUrlTuple = makeCategoryUrlTuple(body, false)
      val rowsFuture = client.preparedQuery(query).execute(categoryUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val categoryUrlTuple = makeCategoryUrlTuple(body, true)
      val rowsFuture = client.preparedQuery(query).execute(categoryUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val rowsFuture = client.preparedQuery(query).execute(makeCategoryUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
            productUrlsList.add(makeProductUrl(row))
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
      val rowsFuture = client.preparedQuery(query).execute(makeProductUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(makeProductUrl(rows.first()), productUrlsDeliveryOptions)
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
      val productUrlTuple = makeProductUrlTuple(body, false)
      val rowsFuture = client.preparedQuery(query).execute(productUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val productUrlTuple = makeProductUrlTuple(body, true)
      val rowsFuture = client.preparedQuery(query).execute(productUrlTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val rowsFuture = client.preparedQuery(query).execute(makeProductUrlTuple(body, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { res ->
        if (res.succeeded()) {
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
      val joinList = checkJoinMessage(body.joinList)
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
            fullUrlKeysList.add(makeFullUrlKey(row, joinList))
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
      val joinList = checkJoinMessage(body.joinList)
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
          message.reply(makeFullUrlKey(rows.first(), joinList), fullUrlsDeliveryOptions)
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
  private fun makeUrlKey(row: Row): UrlKeys {
    return  UrlKeys(
      urlKey = row.getString("url_key"),
      upperKey = row.getString("upper_key"),
      pageType = convertStringToPageType(row.getString("page_type"))
    )
  }

  /**
   * Create JSON fields for the text page urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the database
   */
  private fun makeTextPageUrl(row: Row): TextPageUrls {
    return TextPageUrls(
      urlKeys = row.getString("url_key"),
      upperKey = row.getString("upper_key"),
      textPagesId = row.getInteger("text_pages_id")
    )
  }

  /**
   * Create JSON fields for the category urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the database
   */
  private fun makeCategoryUrl(row: Row): CategoryUrls {
    return CategoryUrls(
      urlKeys = row.getString("url_key"),
      upperKey = row.getString("upper_key"),
      categoryId = row.getInteger("category_id"),
    )
  }

  /**
   * Create JSON fields for the product urls
   *
   * @param row The row from the database
   * @return A list with the converted fields from the databas
   */
  private fun makeProductUrl(row: Row): ProductUrls {
    return ProductUrls(
      urlKeys = row.getString("url_key"),
      upperKey = row.getString("upper_key"),
      productId = row.getInteger("product_id"),
    )
  }

  /**
   * Create JSON fields for the full url key query
   *
   * @param row The row from the database
   * @param joinList A list of booleans indicating whether to join with text_pages, categories, or products
   * @return The constructed query string
   */
  private fun makeFullUrlKey(row: Row, joinList: List<Boolean>): FullUrlKeys {
    val fullUrlKeys = FullUrlKeys(
      urlKeys = makeUrlKey(row),
      textPage = null,
      category = null,
      product = null
    )

    if (joinList[0]) {
      val textPages = TextPages(
        textPagesId = row.getInteger("text_pages_id"),
        name = row.getString("text_page_name"),
        shortText = row.getString("text_page_short_text"),
        text = row.getString("text_page_text")
      )

      fullUrlKeys.textPage = textPages
    }

    if (joinList[1]) {
      val category = Categories(
        categoryId = row.getInteger("category_id"),
        upperCategory = row.getInteger("upper_category"),
        name = row.getString("category_name"),
        description = row.getString("category_description"),
        shortDescription = row.getString("category_short_description")
      )

      fullUrlKeys.category = category
    }

    if (joinList[2]) {
      val product = Products(
        productId = row.getInteger("product_id"),
        name = row.getString("product_name"),
        shortName = row.getString("product_short_name"),
        description = row.getString("product_description"),
        shortDescription = row.getString("product_short_description"),
        sku = row.getString("product_sku"),
        ean = row.getString("product_ean"),
        manufacturer = row.getString("product_manufacturer"),
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
  private fun makeUrlKeyTuple(body: UrlKeys, isPutRequest: Boolean): Tuple {
    val urlKeyTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        convertPageTypeToString(body.pageType),
        body.urlKey,
        body.upperKey,
      )
    } else {
      Tuple.of(
        body.urlKey,
        body.upperKey,
        convertPageTypeToString(body.pageType),
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
  private fun makeTextPageUrlTuple(body: TextPageUrls, isPutRequest: Boolean): Tuple {
    val textPageUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.textPagesId,
        body.urlKeys,
        body.upperKey,
        body.textPagesId,
      )
    } else {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.textPagesId,
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
  private fun makeCategoryUrlTuple(body: CategoryUrls, isPutRequest: Boolean): Tuple {
    val categoryUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.categoryId,
        body.urlKeys,
        body.upperKey,
        body.categoryId,
      )
    } else {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.categoryId,
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
  private fun makeProductUrlTuple(body: ProductUrls, isPutRequest: Boolean): Tuple {
    val productUrlTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.productId,
        body.urlKeys,
        body.upperKey,
        body.productId,
      )
    } else {
      Tuple.of(
        body.urlKeys,
        body.upperKey,
        body.productId,
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
  private fun checkJoinMessage(body: JoinList): MutableList<Boolean> {
    val joinList: MutableList<Boolean> = mutableListOf(false, false, false)

    joinList[0]= try {
      body.joinTextPage
    } catch (e: NullPointerException) { false}
    joinList[1] = try {
      body.joinCategory
    } catch (e: NullPointerException) { false }
    joinList[2] = try {
      body.joinProduct
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

  private fun convertPageTypeToString(pageType: PageType): String {
    return when (pageType) {
      PageType.TEXT_PAGE -> "text_page"
      PageType.CATEGORY -> "category"
      PageType.PRODUCT -> "product"
    }
  }

  private fun convertStringToPageType(pageType: String): PageType {
    return when (pageType) {
      "text_page" -> PageType.TEXT_PAGE
      "category" -> PageType.CATEGORY
      "product" -> PageType.PRODUCT
      else -> throw IllegalArgumentException("Invalid page type: $pageType")
    }
  }
}
