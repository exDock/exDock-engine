package com.ex_dock.ex_dock.database.product

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import com.ex_dock.ex_dock.helper.replyListMessage
import com.ex_dock.ex_dock.helper.replySingleMessage
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class ProductJdbcVerticle: VerticleBase() {
  private lateinit var client: MongoClient
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val productDeliveryOptions = DeliveryOptions().setCodecName("ProductInfoCodec")

  companion object {
    private const val CACHE_ADDRESS = "products"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections to the product table
    getAllProducts()
    getProductById()
    createProduct()
    updateProduct()
    deleteProduct()

    return Future.succeededFuture<Unit>()
  }

  private fun getAllProducts() {
    val getAllProductsConsumer = eventBus.consumer<String>("process.product.getAllProducts")
    getAllProductsConsumer.handler { message ->
      val query = JsonObject()
      client.find("products", query).replyListMessage(message)
    }
  }

  private fun getProductById() {
    val getProductByIdConsumer = eventBus.consumer<String>("process.product.getProductById")
    getProductByIdConsumer.handler { message ->
      val productId = message.body()
      val query = JsonObject()
        .put("_id", productId)
      client.find("products", query).replySingleMessage(message)
    }
  }

  private fun createProduct() {
    val createProductConsumer = eventBus.consumer<ProductInfo>("process.product.createProduct")
    createProductConsumer.handler { message ->
      val product = message.body()
      val document = product.toDocument()

      val rowsFuture = client.save("products", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        product.productId = lastInsertID

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(product, productDeliveryOptions)
      }
    }
  }

  private fun updateProduct() {
    val updateProductConsumer = eventBus.consumer<ProductInfo>("process.product.updateProduct")
    updateProductConsumer.handler { message ->
      val body = message.body()

      if (body.productId == null) {
        message.fail(400, failedMessage)
        return@handler
      }

      val document = body.toDocument()
      val rowsFuture = client.save("products", document)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
        }

      rowsFuture.onSuccess { res ->
        val lastInsertID: String = res
        body.productId = lastInsertID

        setCacheFlag(eventBus, CACHE_ADDRESS)
        message.reply(body, productDeliveryOptions)
      }
    }
  }

  private fun deleteProduct() {
    val deleteProductConsumer = eventBus.consumer<String>("process.product.deleteProduct")
    deleteProductConsumer.handler { message ->
      val productId = message.body()
      val query = JsonObject()
        .put("_id", productId)

      val rowsFuture = client.removeDocument("products", query)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.fail(400, failedMessage)
      }

      rowsFuture.onSuccess { res ->
        message.reply("Product deleted successfully")
      }
    }
  }
}
