package com.ex_dock.ex_dock.database.image

import com.ex_dock.ex_dock.database.connection.getConnection
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class ImageJdbcVerticle: VerticleBase() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")
  private val imageDeliveryOptions = DeliveryOptions().setCodecName("ImageCodec")
  private val imageProductDeliveryOptions = DeliveryOptions().setCodecName("ImageProductCodec")

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections for basic images
    getAllImages()
    getImageByUrl()
    createImage()
    editImage()
    deleteImage()

    // Initialize all eventbus connections for image products
    getAllImageProducts()
    getImageProductsByImageUrl()
    getImageProductsByProductId()
    createImageProduct()
    editImageProduct()
    deleteImageProduct()

    return Future.succeededFuture<Unit>()
  }

  /**
   * Retrieves all images from the database and sends them as a JSON object to the specified EventBus address.
   *
   * @return None
   */
  private fun getAllImages() {
    val getAllImagesConsumer = eventBus.localConsumer<Unit>("process.images.getAll")
    getAllImagesConsumer.handler { message ->
      val query = "SELECT * FROM image"
      val rowsFuture = client.preparedQuery(query).execute()
      val imageList: MutableList<Image> = emptyList<Image>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            imageList.add(row.makeImage())
          }
        }
        message.reply(imageList, listDeliveryOptions)
      }
    }
  }

  /**
   * Retrieves an image from the database based on the provided image URL.
   *
   * @return None
   */
  private fun getImageByUrl() {
    val getImageByUrlConsumer = eventBus.localConsumer<String>("process.images.getByUrl")
    getImageByUrlConsumer.handler { message ->
      val query = "SELECT * FROM image WHERE image_url = ?"
      val url = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(url))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ res: RowSet<Row> ->
        if (res.size() > 0) {
          message.reply(res.first().makeImage(), imageDeliveryOptions)
        } else {
          message.reply("No image found")
        }
      }
    }
  }

  /**
   * Create a new image entry in the database
   */
  private fun createImage() {
    val createImageConsumer = eventBus.localConsumer<Image>("process.images.create")
    createImageConsumer.handler { message ->
      val query = "INSERT INTO image (image_url, image_name, extensions) VALUES (?,?,?)"
      val image = message.body()

      val queryTuple: Tuple = image.toTuple()

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(image, imageDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing image in the database
   */
  private fun editImage() {
    val editImageConsumer = eventBus.localConsumer<Image>("process.images.edit")
    editImageConsumer.handler { message ->
      val query = "UPDATE image SET image_name = ?, extensions = ? WHERE image_url = ?"
      val image = message.body()
      val queryTuple = Tuple.of(
        image.imageName,
        image.imageExtensions,
        image.imageUrl
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply(image, imageDeliveryOptions)
      }
    }
  }

  /**
   * Delete an existing image in the database
   */
  private fun deleteImage() {
    val deleteImageConsumer = eventBus.localConsumer<String>("process.images.delete")
    deleteImageConsumer.handler { message ->
      val query = "DELETE FROM image WHERE image_url = ?"
      val url = message.body()

      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(url))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("Image deleted successfully!")
      }
    }
  }

  /**
   * Get all data from the image_product table
   */
  private fun getAllImageProducts() {
    val getAllImageProductsConsumer = eventBus.localConsumer<Unit>("process.images.getAllImageProducts")
    getAllImageProductsConsumer.handler { message ->
      val query = "SELECT * FROM image_product"
      val rowsFuture = client.preparedQuery(query).execute()
      val imageProductList: MutableList<ImageProduct> = emptyList<ImageProduct>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            imageProductList.add(row.makeImageProduct())
          }
        }
        message.reply(imageProductList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get the data from the image_product table by image URL
   */
  private fun getImageProductsByImageUrl() {
    val getImageProductsByImageUrlConsumer = eventBus.localConsumer<String>("process.images.getImageProductsByImageUrl")
    getImageProductsByImageUrlConsumer.handler { message ->
      val query = "SELECT * FROM image_product WHERE image_url = ?"
      val url = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(url))
      val imageProductList: MutableList<ImageProduct> = emptyList<ImageProduct>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            imageProductList.add(row.makeImageProduct())
          }
        }
        message.reply(imageProductList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get the data from the image_product table by product ID
   */
  private fun getImageProductsByProductId() {
    val getImageProductsByProductIdConsumer = eventBus.localConsumer<Int>("process.images.getImageProductsByProductId")
    getImageProductsByProductIdConsumer.handler { message ->
      val query = "SELECT * FROM image_product WHERE product_id = ?"
      val id = message.body()
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(id))
      val imageProductList: MutableList<ImageProduct> = emptyList<ImageProduct>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { res: RowSet<Row> ->
        if (res.size() > 0) {
          res.forEach { row ->
            imageProductList.add(row.makeImageProduct())
          }
        }
        message.reply(imageProductList, listDeliveryOptions)
      }
    }
  }

  /**
   * Create a new entry in the image_product table
   */
  private fun createImageProduct() {
    val createImageProductConsumer = eventBus.localConsumer<ImageProduct>("process.images.createImageProduct")
    createImageProductConsumer.handler { message ->
      val query = "INSERT INTO image_product (product_id, image_url) VALUES (?,?)"
      val imageProduct = message.body()
      val queryTuple = Tuple.of(
        imageProduct.productId,
        imageProduct.imageUrl
      )

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess{ _ ->
        message.reply(imageProduct, imageProductDeliveryOptions)
      }
    }
  }

  /**
   * Edit an existing entry in the image_product table
   */
  private fun editImageProduct() {
    val editImageProductConsumer = eventBus.localConsumer<ImageProduct>("process.images.editImageProduct")
    editImageProductConsumer.handler { message ->
      // Since image_url is part of the primary key, we can't update it directly
      // We need to delete the old record and create a new one if the URL changes
      // For now, let's assume we can't modify the association, only delete and recreate
      message.reply("To modify an image-product association, please delete the existing one and create a new one", imageProductDeliveryOptions)
    }
  }

  /**
   * Delete an existing entry in the image_product table
   */
  private fun deleteImageProduct() {
    val deleteImageProductConsumer = eventBus.localConsumer<ImageProduct>("process.images.deleteImageProduct")
    deleteImageProductConsumer.handler { message ->
      val query = "DELETE FROM image_product WHERE product_id = ? AND image_url = ?"
      val imageProduct = message.body()

      val queryTuple = Tuple.of(imageProduct.productId, imageProduct.imageUrl)

      val rowsFuture = client.preparedQuery(query).execute(queryTuple)

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply("Failed to execute query: $res")
      }.onSuccess { _ ->
        message.reply("Image product association deleted successfully!")
      }
    }
  }

  /**
   * Helper function to make JSON fields from a given database row in the image table
   *
   * @param row The row from the database to be converted into JSON
   */
  private fun Row.makeImage(): Image {
    return Image(
      imageUrl = this.getString("image_url"),
      imageName = this.getString("image_name"),
      imageExtensions = this.getString("extensions")
    )
  }

  /**
   * Helper function to make JSON fields from a given database row in the image_product table
   *
   * @param row The row from the database to be converted into JSON
   */
  private fun Row.makeImageProduct(): ImageProduct {
    return ImageProduct(
      productId = this.getInteger("product_id"),
      imageUrl = this.getString("image_url")
    )
  }

  /**
   * Helper function to create a tuple from a given JSON object in the image table
   *
   * @param body The JSON object to be converted into a tuple
   * @return A tuple from the given JSON object
   */
  private fun Image.toTuple(): Tuple {
    return Tuple.of(
      this.imageUrl,
      this.imageName,
      this.imageExtensions
    )
  }
}
