package com.ex_dock.ex_dock.database.category

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.product.Products
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class CategoryJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")
  private val categoriesDeliveryOptions = DeliveryOptions().setCodecName("CategoriesCodec")
  private val seoCategoriesDeliveryOptions = DeliveryOptions().setCodecName("CategoriesSeoCodec")

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for basic categories
    getAllCategories()
    getCategoryById()
    createCategory()
    editCategory()
    deleteCategory()

    // Initialize all eventbus connections for Search Engine Optimization categories
    getAllSeoCategories()
    getSeoCategoryByCategoryId()
    createSeoCategory()
    editSeoCategory()
    deleteSeoCategory()

    // Initialize all eventbus connections for getting Full Category Information
    getAllFullCategoryInfo()
    getFullCategoryInfoByCategoryId()
    getAllProductsByCategoryId()
  }

    /**
   * Retrieves all categories from the database and sends them as a JSON object to the specified EventBus address.
   *
   * @return None
   */
  private fun getAllCategories() {
    val getAllCategoriesConsumer = eventBus.localConsumer<Unit>("process.categories.getAll")
    getAllCategoriesConsumer.handler { message ->
      val query = "SELECT * FROM categories"
      val rowsFuture = client.preparedQuery(query).execute()
      val categoryList: MutableList<Categories> = emptyList<Categories>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            categoryList.add(makeCategory(row))
          }
        }
        message.reply(categoryList, listDeliveryOptions)
      }
    }
  }

  /**
   * Retrieves a category from the database based on the provided category ID.
   *
   * @return None
   */
  private fun getCategoryById() {
    val getCategoryByIdConsumer = eventBus.localConsumer<Int>("process.categories.getById")
    getCategoryByIdConsumer.handler { message ->
      val query = "SELECT * FROM categories WHERE category_id = ?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ res: RowSet<Row> ->
        if (res.size() > 0) {
          message.reply(makeCategory(res.first()), categoriesDeliveryOptions)
        } else {
          message.reply("No category found")
        }
      }
    }
  }

  /**
   * Create a new category entry in the database
   */
  private fun createCategory() {
    val createCategoryConsumer = eventBus.localConsumer<Categories>("process.categories.create")
    createCategoryConsumer.handler { message ->
      val query = "INSERT INTO categories (upper_category, name, short_description, description) VALUES (?,?,?,?)"
      val category = message.body()

      val queryTuple: Tuple = makeCategoryTuple(category, false)

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res ->
        val categoryId = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        category.categoryId = categoryId
        message.reply(category, categoriesDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing category in the database
   */
  private fun editCategory() {
    val editCategoryConsumer = eventBus.localConsumer<Categories>("process.categories.edit")
    editCategoryConsumer.handler { message ->
      val query =
        "UPDATE categories SET upper_category = ?, name = ?" +
          ", short_description = ?, description = ? WHERE category_id = ?"
      val category = message.body()
      val queryTuple = makeCategoryTuple(category, true)

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(category, categoriesDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing category in the database
   */
  private fun deleteCategory() {
    val deleteCategoryConsumer = eventBus.localConsumer<Int>("process.categories.delete")
    deleteCategoryConsumer.handler { message ->
      val query = "DELETE FROM categories WHERE category_id =?"
      val id = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("Category deleted successfully!")
      }
    }
  }

  /**
   * Get all data from the SEO Categories table
   */
  private fun getAllSeoCategories() {
    val getAllSeoCategoriesConsumer = eventBus.localConsumer<Unit>("process.categories.getAllSeo")
    getAllSeoCategoriesConsumer.handler { message ->
      val query = "SELECT * FROM categories_seo"
      val rowsFuture = client.preparedQuery(query).execute()
      val seoCategoryList: MutableList<CategoriesSeo> = emptyList<CategoriesSeo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            seoCategoryList.add(makeSeoCategory(row))
          }
        }
        message.reply(seoCategoryList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get the data from the SEO categories table by category ID
   */
  private fun getSeoCategoryByCategoryId() {
    val getSeoCategoryByCategoryIdConsumer = eventBus.localConsumer<Int>("process.categories.getSeoById")
    getSeoCategoryByCategoryIdConsumer.handler { message ->
      val query = "SELECT * FROM categories_seo WHERE category_id =?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          val row = res.first()
          message.reply(makeSeoCategory(row), seoCategoriesDeliveryOptions)
        } else {
          message.reply("No category SEO found with this id!")
        }
      }
    }
  }

  /**
   * Create a new entry in the SEO categories table
   */
  private fun createSeoCategory() {
    val createSeoCategoryConsumer = eventBus.localConsumer<CategoriesSeo>("process.categories.createSeoCategory")
    createSeoCategoryConsumer.handler { message ->
      val query =
        "INSERT INTO categories_seo (category_id, meta_title, meta_description, meta_keywords, page_index) " +
          "VALUES (?,?,?,?,?::p_index)"
      val categorySeo = message.body()
      val queryTuple = makeSeoCategoryTuple(categorySeo, false)

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ _ ->
        message.reply(categorySeo, seoCategoriesDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing entry in the SEO categories table
   */
  private fun editSeoCategory() {
    val editSeoCategoryConsumer = eventBus.localConsumer<CategoriesSeo>("process.categories.editSeoCategory")
    editSeoCategoryConsumer.handler { message ->
      val query =
        "UPDATE categories_seo SET meta_title =?, meta_description =?, meta_keywords =?, page_index =?::p_index " +
          "WHERE category_id =?"
      val categorySeo = message.body()

      val queryTuple = makeSeoCategoryTuple(categorySeo, true)

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(categorySeo, seoCategoriesDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing entry in the SEO categories table
   */
  private fun deleteSeoCategory() {
    val deleteSeoCategoryConsumer = eventBus.localConsumer<Int>("process.categories.deleteSeoCategory")
    deleteSeoCategoryConsumer.handler { message ->
      val query = "DELETE FROM categories_seo WHERE category_id =?"
      val id = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("SEO category deleted successfully!")
      }
    }
  }

  /**
   * Get all data from the SEO Categories and Categories tables with full category info
   */
  private fun getAllFullCategoryInfo() {
    val getAllFullCategoryInfoConsumer = eventBus.localConsumer<Unit>("process.categories.getAllFullInfo")
    getAllFullCategoryInfoConsumer.handler { message ->
      val query =
        "SELECT c.category_id, c.upper_category, c.name, c.short_description, c.description, cs.meta_title" +
          ", cs.meta_description, cs.meta_keywords, cs.page_index FROM categories c " +
          "LEFT JOIN categories_seo cs ON c.category_id = cs.category_id"
      val rowsFuture = client.preparedQuery(query).execute()
      val fullCategoryList: MutableList<FullCategoryInfo> = emptyList<FullCategoryInfo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            fullCategoryList.add(makeFullCategoryInfo(row))
          }
        }
        message.reply(fullCategoryList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get the data from the SEO Categories and Categories tables with full category info by category ID
   */
  private fun getFullCategoryInfoByCategoryId() {
    val getFullCategoryInfoByCategoryIdConsumer = eventBus.localConsumer<Int>("process.categories.getFullInfoById")
    getFullCategoryInfoByCategoryIdConsumer.handler { message ->
      val query =
        "SELECT c.category_id, c.upper_category, c.name, c.short_description, c.description, cs.meta_title" +
          ", cs.meta_description, cs.meta_keywords, cs.page_index FROM categories c " +
          "LEFT JOIN categories_seo cs ON c.category_id = cs.category_id WHERE c.category_id =?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res ->
        if (res.size() > 0) {
          val row = res.first()
          message.reply(makeFullCategoryInfo(row), categoriesDeliveryOptions)
        } else {
          message.reply("No category found")
        }
      }
    }
  }

  /**
   * Retrieves all products associated with the given category ID from the database.
   */
  private fun getAllProductsByCategoryId() {
    val getAllProductsByCategoryIdConsumer = eventBus.localConsumer<Int>("process.categories.getAllProductsByCategoryId")
    getAllProductsByCategoryIdConsumer.handler { message ->
      val query =
        "SELECT p.product_id, p.name AS product_name, p.short_name AS product_short_name, " +
          "p.description AS product_description, p.short_description AS product_short_description, " +
          "p.sku AS product_sku, p.ean AS product_ean, p.manufacturer AS product_manufacturer, " +
          "c.category_id, c.upper_category, c.name, c.short_description, c.description " +
          "FROM products p JOIN categories_products cp ON cp.product_id = p.product_id " +
          "JOIN categories c ON cp.category_id = c.category_id WHERE c.category_id = ?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))
      val productList: MutableList<CategoriesProducts> = emptyList<CategoriesProducts>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            productList.add(makeCategoriesProducts(row))
          }
        }
        message.reply(productList, listDeliveryOptions)
      }
    }
  }

  /**
   * Helper function to make JSON fields from a given database row in the category table
   *
   * @param row The row from the database to be converted into JSON
   */
  private fun makeCategory(row: Row): Categories {
    return Categories(
      categoryId = row.getInteger("category_id"),
      upperCategory = row.getInteger("upper_category"),
      name = row.getString("name"),
      shortDescription = row.getString("short_description"),
      description = row.getString("description")
      )
  }

  /**
   * Helper function to make JSON fields from a given database row in the categories_seo table
   *
   * @param row The row from the database to be converted into JSON
   */
  private fun makeSeoCategory(row: Row): CategoriesSeo {
    return CategoriesSeo(
      categoryId = row.getInteger("category_id"),
      metaTitle = row.getString("meta_title"),
      metaDescription = row.getString("meta_description"),
      metaKeywords = row.getString("meta_keywords"),
      pageIndex = getEnum(row.getString("page_index"))
    )
  }

  /**
   * Helper function to make JSON fields from a given database row in the full category info table
   *
   * @param row The row from the database to be converted into JSON
   */
  private fun makeFullCategoryInfo(row: Row): FullCategoryInfo {
    return FullCategoryInfo(
      makeCategory(row),
      makeSeoCategory(row)
    )
  }

    /**
   * Creates a list of JSON fields from a given database row in the categories_products table.
   *
   * @param row The row from the database to be converted into JSON
   * @return A list of key-value pairs representing the JSON fields for the given row
   */
  private fun makeCategoriesProducts(row: Row): CategoriesProducts {
    return CategoriesProducts(
      makeCategory(row),
      Products(
        productId = row.getInteger("product_id"),
        name = row.getString("product_name"),
        shortName = row.getString("product_short_name"),
        description = row.getString("product_description"),
        shortDescription = row.getString("product_short_description"),
        sku = row.getString("product_sku"),
        ean = row.getString("product_ean"),
        manufacturer = row.getString("product_manufacturer")
      )
    )
  }

  /**
   * Helper function to create a tuple from a given JSON object in the category table
   *
   * @param body The JSON object to be converted into a tuple
   * @return A tuple from the given JSON object
   */
  private fun makeCategoryTuple(body: Categories, putRequest: Boolean): Tuple {
    val categoryTuple: Tuple

    if (putRequest) {
      categoryTuple = Tuple.of(
        body.upperCategory,
        body.name,
        body.shortDescription,
        body.description,
        body.categoryId,
      )
    } else {
      categoryTuple = Tuple.of(
        body.upperCategory,
        body.name,
        body.shortDescription,
        body.description,
      )
    }

    return categoryTuple
  }

  /**
   * Helper function to create a tuple from a given JSON object in the categories_seo table
   *
   * @param body The JSON object to be converted into a tuple
   * @return A tuple from the given JSON object
   */
  private fun makeSeoCategoryTuple(body: CategoriesSeo, putRequest: Boolean): Tuple {
    val categorySeoTuple: Tuple

    if (putRequest) {
      categorySeoTuple = Tuple.of(
        body.metaTitle,
        body.metaDescription,
        body.metaKeywords,
        convertEnum(body.pageIndex),
        body.categoryId,
      )
    } else {
      categorySeoTuple = Tuple.of(
        body.categoryId,
        body.metaTitle,
        body.metaDescription,
        body.metaKeywords,
        convertEnum(body.pageIndex)
      )
    }

    return categorySeoTuple
  }

  private fun getEnum(name: String): PageIndex {
    when (name) {
      "index, follow" -> return PageIndex.IndexFollow
      "index, nofollow" -> return PageIndex.IndexNoFollow
      "noindex, follow" -> return PageIndex.NoIndexFollow
      "noindex, nofollow" -> return PageIndex.NoIndexNoFollow
    }

    return PageIndex.NoIndexNoFollow
  }

  private fun convertEnum(pageIndex: PageIndex): String {
    return when (pageIndex) {
      PageIndex.IndexFollow -> "index, follow"
      PageIndex.IndexNoFollow -> "index, nofollow"
      PageIndex.NoIndexFollow -> "noindex, follow"
      PageIndex.NoIndexNoFollow -> "noindex, nofollow"
    }
  }
}
