package com.ex_dock.ex_dock.backend.v1.router.products

import com.ex_dock.ex_dock.database.category.toPageIndex
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.product.toDocument
import com.ex_dock.ex_dock.helper.ctx.errorResponse
import com.ex_dock.ex_dock.helper.ctx.jsonResponse
import com.ex_dock.ex_dock.helper.futures.addFuture
import com.ex_dock.ex_dock.helper.json.toList
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun Router.initProductsRouter(vertx: Vertx) {
  val productsRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  val productInfoDeliveryOptions = DeliveryOptions().setCodecName("ProductInfoCodec")

  productsRouter.get("/overview").handler { ctx ->
    val allFutures: MutableList<Future<Unit>> = mutableListOf()
    lateinit var products: JsonArray
    var columns: JsonArray? = null
    var filters: JsonArray? = null
    var bulkActions: JsonArray? = null

    allFutures.addFuture { promise ->
      eventBus.request<List<JsonObject>>("process.product.getAllProducts", "").onFailure { err ->
        promise.fail(err)
      }.onSuccess { message ->
        products = JsonArray(message.body())
        promise.complete()
      }
    }

    if (ctx.queryParam("columns").firstOrNull() == "1") allFutures.addFuture { promise ->
      eventBus.request<JsonArray>("process.product.getColumns", "").onFailure { err ->
        promise.fail(err)
      }.onSuccess { message ->
        columns = message.body()
        promise.complete()
      }
    }

    Future.all<Unit>(allFutures).onFailure { err ->
      ctx.errorResponse(err)
    }.onSuccess {
      val result = mutableMapOf<String, Any>()

      result["products"] = products
      if (columns != null) result["columns"] = columns
      if (filters != null) result["filters"] = filters
      if (bulkActions != null) result["bulkActions"] = bulkActions

      ctx.jsonResponse(JsonObject(result))
    }
  }

  productsRouter.post("/").handler { ctx ->
    val bodyJson = ctx.body().asJsonObject()

    println("Create product POST route is reached")

    if (bodyJson == null) {
      ctx.errorResponse(400, "Add product (router): missing json in body")
      throw IllegalArgumentException("Add product (router): missing bodyJson")
    }

    val productInfo = ProductInfo(
      productId = null,
      name = bodyJson.getString("name")
        ?: throw IllegalArgumentException("Add product (router): missing 'name' key in bodyJson"),
      shortName = bodyJson.getString("shortName")
        ?: throw IllegalArgumentException("Add product (router): missing 'shortName' key in bodyJson"),
      description = bodyJson.getString("description")
        ?: throw IllegalArgumentException("Add product (router): missing 'description' key in bodyJson"),
      shortDescription = bodyJson.getString("shortDescription")
        ?: throw IllegalArgumentException("Add product (router): missing 'shortDescription' key in bodyJson"),
      sku = bodyJson.getString("sku")
        ?: throw IllegalArgumentException("Add product (router): missing 'sku' key in bodyJson"),
      ean = bodyJson.getString("ean")
        ?: throw IllegalArgumentException("Add product (router): missing 'ean' key in bodyJson"),
      location = bodyJson.getString("location")
        ?: throw IllegalArgumentException("Add product (router): missing 'location' key in bodyJson"),
      manufacturer = bodyJson.getString("manufacturer")
        ?: throw IllegalArgumentException("Add product (router): missing 'manufacturer' key in bodyJson"),
      metaTitle = bodyJson.getString("metaTitle")
        ?: throw IllegalArgumentException("Add product (router): missing 'metaTitle' key in bodyJson"),
      metaDescription = bodyJson.getString("metaDescription")
        ?: throw IllegalArgumentException("Add product (router): missing 'metaDescription' key in bodyJson"),
      metaKeywords = bodyJson.getString("metaKeywords")
        ?: throw IllegalArgumentException("Add product (router): missing 'metaKeywords' key in bodyJson"),
      pageIndex = bodyJson.getString("pageIndex")?.toPageIndex()
        ?: throw IllegalArgumentException("Add product (router): missing 'pageIndex' key in bodyJson"),
      price = bodyJson.getDouble("price")
        ?: throw IllegalArgumentException("Add product (router): missing 'price' key in bodyJson"),
      salePrice = bodyJson.getDouble("salePrice")
        ?: throw IllegalArgumentException("Add product (router): missing 'salePrice' key in bodyJson"),
      costPrice = bodyJson.getDouble("costPrice")
        ?: throw IllegalArgumentException("Add product (router): missing 'costPrice' key in bodyJson"),
      taxClass = bodyJson.getString("taxClass")
        ?: throw IllegalArgumentException("Add product (router): missing 'taxClass' key in bodyJson"),
      saleDates = bodyJson.getJsonArray("saleDates")?.toList() ?: emptyList(),
      categories = bodyJson.getJsonArray("categories")?.toList() ?: emptyList(),
      attributes = bodyJson.getJsonArray("attributes")?.toList() ?: emptyList(),
      images = bodyJson.getJsonArray("images")?.toList() ?: emptyList(),
    )

    eventBus.request<ProductInfo>("process.product.createProduct", productInfo, productInfoDeliveryOptions)
      .onFailure { err ->
        ctx.errorResponse(err)
      }.onSuccess { res ->
        ctx.jsonResponse(res.body().toDocument())
      }
  }

  productsRouter.singleProduct(eventBus)

  this.route("/products*").subRouter(productsRouter)
}
