package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.category.PageIndex
import com.ex_dock.ex_dock.database.category.convertToString
import com.ex_dock.ex_dock.database.category.toPageIndex
import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.database.image.Image
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
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

  companion object {
    private const val CACHE_ADDRESS = "products"
  }

  override fun start() {
    client = vertx.getConnection()
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
            productList.add(row.makeProduct())
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
          message.reply(row.makeProduct(), productDeliveryOptions)
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
      val rowsFuture = client.preparedQuery("INSERT INTO products (name, short_name, description, short_description, sku, ean, manufacturer) VALUES (?,?,?,?,?,?,?)")
       .execute(product.toTuple(false))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        val productId: Int = res.value().property(JDBCPool.GENERATED_KEYS).getInteger(0)
        product.productId = productId
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(product, productDeliveryOptions)
      }
    }
  }

  private fun updateProduct() {
    val updateProductConsumer = eventBus.localConsumer<Products>("process.products.updateProduct")
    updateProductConsumer.handler { message ->
      val product = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products SET name =?, short_name =?, description =?, " +
        "short_description =?, sku=?, ean=?, manufacturer=? WHERE product_id =?")
       .execute(product.toTuple(true))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess{ res ->
        if (res.rowCount() > 0) {
          setCacheFlag(eventBus, CACHE_ADDRESS)
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
          setCacheFlag(eventBus, CACHE_ADDRESS)
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
            productsSeoList.add(row.makeProductSeo())
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
          message.reply(row.makeProductSeo(), productSeoDeliveryOptions)
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
       .execute(productSeo.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(productSeo, productSeoDeliveryOptions)
      }
    }
  }

  private fun updateProductSeo() {
    val updateProductSeoConsumer = eventBus.localConsumer<ProductsSeo>("process.products.updateProductSeo")
    updateProductSeoConsumer.handler { message ->
      val productSeo = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products_seo SET meta_title =?, meta_description =?, meta_keywords =?, page_index =?::p_index WHERE product_id =?")
       .execute(productSeo.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
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
        setCacheFlag(eventBus, CACHE_ADDRESS)
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
          productsPricingList.add(row.makeProductsPricing())
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
          message.reply(row.makeProductsPricing(), productPricingDeliveryOptions)
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
      val rowsFuture = client.preparedQuery("INSERT INTO products_pricing (product_id, price, sale_price, " +
        "cost_price, tax_class, sale_date_start, sale_date_end) VALUES (?,?,?,?,?,?,?)")
       .execute(productPricing.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(productPricing, productPricingDeliveryOptions)
      }
    }
  }

  private fun updateProductPricing() {
    val updateProductPricingConsumer = eventBus.localConsumer<ProductsPricing>("process.products.updateProductPricing")
    updateProductPricingConsumer.handler { message ->
      val productPricing = message.body()
      val rowsFuture = client.preparedQuery("UPDATE products_pricing SET price =?, sale_price =?, " +
        "cost_price =?, tax_class=?, sale_date_start=?, sale_date_end=? WHERE product_id =?")
       .execute(productPricing.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onSuccess { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS)
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
        setCacheFlag(eventBus, CACHE_ADDRESS)
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
            val currentProduct = row.makeFullProducts()

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
        "LEFT JOIN public.image_product ip ON products.product_id = ip.product_id " +
        "LEFT JOIN public.image i ON ip.image_url = i.image_url " +
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
            val currentProduct = row.makeFullProducts()

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

  private fun Row.makeProduct(): Products {
    return Products(
      productId = this.getInteger("product_id"),
      name = this.getString("name"),
      shortName = this.getString("short_name"),
      description = this.getString("description"),
      shortDescription = this.getString("short_description"),
      sku = this.getString("sku"),
      ean = this.getString("ean"),
      manufacturer = this.getString("manufacturer"),
    )
  }

  private fun Row.makeProductSeo(): ProductsSeo {
    return ProductsSeo(
      productId = this.getInteger("product_id"),
      metaTitle = this.getString("meta_title"),
      metaDescription = this.getString("meta_description"),
      metaKeywords = this.getString("meta_keywords"),
      pageIndex = this.getString("page_index").toPageIndex()
    )
  }

  private fun Row.makeProductsPricing(): ProductsPricing {
    return ProductsPricing(
      productId = this.getInteger("product_id"),
      price = this.getDouble("price"),
      salePrice = this.getDouble("sale_price"),
      costPrice = this.getDouble("cost_price"),
      taxClass = this.getString("tax_class"),
      saleDateStart = try {this.getString("sale_date_start")} catch (_: Exception) {null},
      saleDateEnd = try {this.getString("sale_date_end")} catch (_: Exception) {null}
    )
  }

  private fun Row.makeFullProducts(): FullProductEntry {
    return FullProductEntry(
      this.makeProduct(),
      this.makeProductSeo(),
      this.makeProductsPricing(),
      Image(
        this.getString("image_url"),
        this.getString("image_name"),
        this.getString("extensions")
      )
    )
  }

  private fun Products.toTuple(isPutRequest: Boolean): Tuple {
    val productTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.name,
        this.shortName,
        this.description,
        this.shortDescription,
        this.sku,
        this.ean,
        this.manufacturer,
        this.productId
      )
    } else {
      Tuple.of(
        this.name,
        this.shortName,
        this.description,
        this.shortDescription,
        this.sku,
        this.ean,
        this.manufacturer,
      )
    }

    return productTuple
  }

  private fun ProductsSeo.toTuple(isPutRequest: Boolean): Tuple {
    val productSeoTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.metaTitle,
        this.metaDescription,
        this.metaKeywords,
        this.pageIndex.convertToString(),
        this.productId
      )
    } else {
      Tuple.of(
        this.productId,
        this.metaTitle,
        this.metaDescription,
        this.metaKeywords,
        this.pageIndex.convertToString(),
      )
    }

    return productSeoTuple
  }

  private fun ProductsPricing.toTuple(isPutRequest: Boolean): Tuple {
    val productsPricingTuple: Tuple = if (isPutRequest) {
      Tuple.of(
        this.price,
        this.salePrice,
        this.costPrice,
        this.taxClass,
        this.saleDateStart,
        this.saleDateEnd,
        this.productId,
      )
    } else {
      Tuple.of(
        this.productId,
        this.price,
        this.salePrice,
        this.costPrice,
        this.taxClass,
        this.saleDateStart,
        this.saleDateEnd
      )
    }

    return productsPricingTuple
  }
}
