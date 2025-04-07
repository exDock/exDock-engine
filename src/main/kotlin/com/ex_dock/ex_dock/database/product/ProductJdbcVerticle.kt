package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.image.Image
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class ProductJdbcVerticle: AbstractVerticle() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductsCodec")
  private val productSeoDeliveryOptions = DeliveryOptions().setCodecName("ProductsSeoCodec")
  private val productPricingDeliveryOptions = DeliveryOptions().setCodecName("ProductsPricingCodec")
  private val fullProductDeliveryOptions = DeliveryOptions().setCodecName("FullProductCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  override fun start() {
    client = getConnection(vertx)
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections to the product table
    getAllProducts()
    getProductById()
    createProduct()
    updateProduct()
    deleteProduct()

    // Initialize all eventbus connections to the products_seo table
    getAllProductsSeo()
    getProductSeoById()
    createProductSeo()
    updateProductSeo()
    deleteProductSeo()

    // Initialize all eventbus connections to the products_pricing table
    getAllProductsPricing()
    getProductPricingById()
    createProductPricing()
    updateProductPricing()
    deleteProductPricing()

    // Initialize all eventbus connections to the full products info tables
    getAllFullProducts()
    getFullProductById()
  }

  private fun getAllProducts() {
    val allProductsConsumer = eventBus.localConsumer<JsonObject>("process.products.getAllProducts")
    allProductsConsumer.handler { message ->
      val rowsFuture = client.preparedQuery("SELECT * FROM products").execute()
      val productList: MutableList<Products> = emptyList<Products>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        if (rows.size() > 0) {
          rows.forEach { row ->
            productList.add(makeProduct(row))
          }
        }

        message.reply(productList, listDeliveryOptions)
      }
    }
  }

  private fun getProductById() {
    val getByIdConsumer = eventBus.localConsumer<Int>("process.products.getProductById")
    getByIdConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("SELECT * FROM products WHERE product_id = ?")
        .execute(Tuple.of(productId))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        val rows: RowSet<Row> = res
        if (rows.size() > 0) {
          val row = rows.first()
          message.reply(makeProduct(row), productDeliveryOptions)
        } else {
          message.reply("No product found!")
        }
      }
    }
  }

  private fun createProduct() {
    val createProductConsumer = eventBus.localConsumer<Products>("process.products.createProduct")
    createProductConsumer.handler { message ->
      val product = message.body()
      val rowsFuture = client.preparedQuery("INSERT INTO products (name, short_name, description, short_description) VALUES (?,?,?,?)")
       .execute(makeProductTuple(product, false))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        val productId: Int = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        product.productId = productId
        message.reply(product, productDeliveryOptions)
      }
    }
  }

  private fun updateProduct() {
    val updateProductConsumer = eventBus.localConsumer<Products>("process.products.updateProduct")
    updateProductConsumer.handler { message ->
      val product = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products SET name =?, short_name =?, description =?, short_description =? WHERE product_id =?")
       .execute(makeProductTuple(product, true))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        if (res.rowCount() > 0) {
          message.reply(product, productDeliveryOptions)
        } else {
          message.reply("Failed to update product")
        }
      }
    }
  }

  private fun deleteProduct() {
    val deleteProductConsumer = eventBus.localConsumer<Int>("process.products.deleteProduct")
    deleteProductConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("DELETE FROM products WHERE product_id =?")
       .execute(Tuple.of(productId))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        if (res.rowCount() > 0) {
          message.reply("Product deleted successfully")
        } else {
          message.reply("Failed to delete product")
        }
      }
    }
  }

  private fun getAllProductsSeo() {
    val allProductSeoConsumer = eventBus.localConsumer<JsonObject>("process.products.getAllProductsSeo")
    allProductSeoConsumer.handler { message ->
      val rowsFuture = client.preparedQuery("SELECT * FROM products_seo").execute()
      val productsSeoList: MutableList<ProductsSeo> = emptyList<ProductsSeo>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        if (rows.size() > 0) {
          rows.forEach { row ->
            productsSeoList.add(makeProductSeo(row))
          }
        }

        message.reply(productsSeoList, listDeliveryOptions)
      }
    }
  }

  private fun getProductSeoById() {
    val getProductSeoByIdConsumer = eventBus.localConsumer<Int>("process.products.getProductSeoById")
    getProductSeoByIdConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("SELECT * FROM products_seo WHERE product_id =?")
        .execute(Tuple.of(productId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        if (rows.size() > 0) {
          val row = rows.first()
          message.reply(makeProductSeo(row), productSeoDeliveryOptions)
        } else {
          message.reply("No products were found!")
        }
      }
    }
  }

  private fun createProductSeo() {
    val createProductSeoConsumer = eventBus.localConsumer<ProductsSeo>("process.products.createProductSeo")
    createProductSeoConsumer.handler { message ->
      val productSeo = message.body()
      val rowsFuture = client.preparedQuery("INSERT INTO products_seo (product_id, meta_title, meta_description, meta_keywords, page_index) VALUES (?,?,?,?,?::p_index)")
       .execute(makeProductSeoTuple(productSeo, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply(productSeo, productSeoDeliveryOptions)
      }
    }
  }

  private fun updateProductSeo() {
    val updateProductSeoConsumer = eventBus.localConsumer<ProductsSeo>("process.products.updateProductSeo")
    updateProductSeoConsumer.handler { message ->
      val productSeo = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products_seo SET meta_title =?, meta_description =?, meta_keywords =?, page_index =?::p_index WHERE product_id =?")
       .execute(makeProductSeoTuple(productSeo, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply(productSeo, productSeoDeliveryOptions)
      }
    }
  }

  private fun deleteProductSeo() {
    val deleteProductSeoConsumer = eventBus.localConsumer<Int>("process.products.deleteProductSeo")
    deleteProductSeoConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("DELETE FROM products_seo WHERE product_id =?")
       .execute(Tuple.of(productId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply("Product SEO deleted successfully")
      }
    }
  }

  private fun getAllProductsPricing() {
    val allProductsPricingConsumer = eventBus.localConsumer<JsonObject>("process.products.getAllProductsPricing")
    allProductsPricingConsumer.handler { message ->
      val rowsFuture = client.preparedQuery("SELECT * FROM products_pricing").execute()
      val productsPricingList: MutableList<ProductsPricing> = emptyList<ProductsPricing>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        rows.forEach { row ->
          productsPricingList.add(makeProductsPricing(row))
        }

        message.reply(productsPricingList, listDeliveryOptions)
      }
    }
  }

  private fun getProductPricingById() {
    val getProductPricingByIdConsumer = eventBus.localConsumer<Int>("process.products.getProductPricingById")
    getProductPricingByIdConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("SELECT * FROM products_pricing WHERE product_id =?")
        .execute(Tuple.of(productId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        if (rows.size() > 0) {
          val row = rows.first()
          message.reply(makeProductsPricing(row), productPricingDeliveryOptions)
        } else {
          message.reply("No products found!")
        }
      }
    }
  }

  private fun createProductPricing() {
    val createProductPricingConsumer = eventBus.localConsumer<ProductsPricing>("process.products.createProductPricing")
    createProductPricingConsumer.handler { message ->
      val productPricing = message.body()
      val rowsFuture = client.preparedQuery("INSERT INTO products_pricing (product_id, price, sale_price, cost_price) VALUES (?,?,?,?)")
       .execute(makeProductsPricingTuple(productPricing, false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply(productPricing, productPricingDeliveryOptions)
      }
    }
  }

  private fun updateProductPricing() {
    val updateProductPricingConsumer = eventBus.localConsumer<ProductsPricing>("process.products.updateProductPricing")
    updateProductPricingConsumer.handler { message ->
      val productPricing = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products_pricing SET price =?, sale_price =?, cost_price =? WHERE product_id =?")
       .execute(makeProductsPricingTuple(productPricing, true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply(productPricing, productPricingDeliveryOptions)
      }
    }
  }

  private fun deleteProductPricing() {
    val deleteProductPricingConsumer = eventBus.localConsumer<Int>("process.products.deleteProductPricing")
    deleteProductPricingConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("DELETE FROM products_pricing WHERE product_id =?")
       .execute(Tuple.of(productId))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        message.reply("Product pricing deleted successfully")
      }
    }
  }

  private fun getAllFullProducts() {
    val allProductInfoConsumer = eventBus.localConsumer<JsonObject>("process.products.getAllFullProducts")
    allProductInfoConsumer.handler { message ->
      val rowsFuture = client.preparedQuery("SELECT * FROM products " +
        "JOIN public.products_pricing pp on products.product_id = pp.product_id " +
        "JOIN public.products_seo ps on products.product_id = ps.product_id " +
        "JOIN public.image_product ip ON products.product_id = ip.product_id " +
        "JOIN public.image i ON ip.image_url = i.image_url").execute()
      val fullProducts: MutableList<FullProduct> = emptyList<FullProduct>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        var product: FullProduct? = null
        if (rows.size() > 0) {
          rows.forEach { row ->
            val currentProduct = makeFullProducts(row)

            if (product == null || currentProduct.product.productId != product!!.product.productId) {
              if (product != null) fullProducts.add(product!!)

              product = FullProduct(
                product = currentProduct.product,
                productsPricing = currentProduct.productsPricing,
                productsSeo = currentProduct.productsSeo,
                images = mutableListOf()
              )
            }
            product!!.images.add(currentProduct.images)
          }
        }
        if (product!= null) fullProducts.add(product!!)

        message.reply(fullProducts, listDeliveryOptions)
      }
    }
  }

  private fun getFullProductById() {
    val allProductInfoByIdConsumer = eventBus.consumer<Int>("process.products.getFullProductsById")
    allProductInfoByIdConsumer.handler { message ->
      val productId = message.body()
      val rowsFuture = client.preparedQuery("SELECT * FROM products " +
        "JOIN public.products_pricing pp on products.product_id = pp.product_id " +
        "JOIN public.products_seo ps on products.product_id = ps.product_id " +
        "JOIN public.image_product ip ON products.product_id = ip.product_id " +
        "JOIN public.image i ON ip.image_url = i.image_url " +
        "WHERE products.product_id =?")
       .execute(Tuple.of(productId))


      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { res ->
        val rows: RowSet<Row> = res
        var product: FullProduct? = null
        if (rows.size() > 0) {
          rows.forEach { row ->
            val currentProduct = makeFullProducts(row)

            if (product == null || currentProduct.product.productId != productId) {
              product = FullProduct(
                product = currentProduct.product,
                productsPricing = currentProduct.productsPricing,
                productsSeo = currentProduct.productsSeo,
                images = mutableListOf()
              )
            }
            product!!.images.add(currentProduct.images)
          }

          message.reply(product, productDeliveryOptions)
        } else {
          message.reply("No products found!")
        }
      }
    }
  }

  private fun makeProduct(row: Row): Products {
    return Products(
      productId = row.getInteger("product_id"),
      name = row.getString("name"),
      shortName = row.getString("short_name"),
      description = row.getString("description"),
      shortDescription = row.getString("short_description"),
      sku = row.getString("sku"),
      ean = row.getString("ean"),
      manufacturer = row.getString("manufacturer"),
    )
  }

  private fun makeProductSeo(row: Row): ProductsSeo {
    return ProductsSeo(
      productId = row.getInteger("product_id"),
      metaTitle = row.getString("meta_title"),
      metaDescription = row.getString("meta_description"),
      metaKeywords = row.getString("meta_keywords"),
      pageIndex = convertStringToPageIndex(row.getString("page_index"))
    )
  }

  private fun makeProductsPricing(row: Row): ProductsPricing {
    var saleDateStart: String? = null
    var saleDateEnd: String? = null

    try {
      saleDateStart = row.getString("sale_date_start")
      saleDateEnd = row.getString("sale_date_end")
    } catch (_: Exception) {}

    return ProductsPricing(
      productId = row.getInteger("product_id"),
      price = row.getDouble("price"),
      salePrice = row.getDouble("sale_price"),
      costPrice = row.getDouble("cost_price"),
      taxClass = row.getString("tax_class"),
      saleDateStart = saleDateStart,
      saleDateEnd = saleDateEnd
    )
  }

  private fun makeFullProducts(row: Row): FullProductEntry {
    return FullProductEntry(
      makeProduct(row),
      makeProductSeo(row),
      makeProductsPricing(row),
      Image(
        row.getString("image_url"),
        row.getString("image_name"),
        row.getString("extensions")
      )
    )
  }

  private fun makeProductTuple(body: Products, isPutRequest: Boolean): Tuple {
    val productTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.name,
        body.shortName,
        body.description,
        body.shortDescription,
        body.productId
      )
    } else {
      Tuple.of(
        body.name,
        body.shortName,
        body.description,
        body.shortDescription,
      )
    }

    return productTuple
  }

  private fun makeProductSeoTuple(body: ProductsSeo, isPutRequest: Boolean): Tuple {
    val productSeoTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.metaTitle,
        body.metaDescription,
        body.metaKeywords,
        convertPageIndexToString(body.pageIndex),
        body.productId
      )
    } else {
      Tuple.of(
        body.productId,
        body.metaTitle,
        body.metaDescription,
        body.metaKeywords,
        convertPageIndexToString(body.pageIndex),
      )
    }

    return productSeoTuple
  }

  private fun makeProductsPricingTuple(body: ProductsPricing, isPutRequest: Boolean): Tuple {
    val productsPricingTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        body.price,
        body.salePrice,
        body.costPrice,
        body.productId
      )
    } else {
      Tuple.of(
        body.productId,
        body.price,
        body.salePrice,
        body.costPrice,
      )
    }

    return productsPricingTuple
  }

  private fun convertPageIndexToString(pageIndex: PageIndex): String {
    return when (pageIndex) {
      PageIndex.NoIndexFollow -> "noindex, follow"
      PageIndex.NoIndexNoFollow -> "noindex, nofollow"
      PageIndex.IndexFollow -> "index, follow"
      PageIndex.IndexNoFollow -> "index, nofollow"
    }
  }

  private fun convertStringToPageIndex(name: String): PageIndex {
    return when (name) {
      "noindex, follow" -> PageIndex.NoIndexFollow
      "noindex, nofollow" -> PageIndex.NoIndexNoFollow
      "index, follow" -> PageIndex.IndexFollow
      "index, nofollow" -> PageIndex.IndexNoFollow
      else -> throw IllegalArgumentException("Invalid page index: $name")
    }
  }
}
