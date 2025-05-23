package com.ex_dock.ex_dock.backend.v1.router

import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.database.backend_block.FullBlockInfo
import com.ex_dock.ex_dock.database.product.FullProduct
import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import com.ex_dock.ex_dock.backend.v1.router.image.initImage
import com.ex_dock.ex_dock.helper.convertJsonElement
import com.ex_dock.ex_dock.helper.findValueByFieldName
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

fun Router.enableBackendV1Router(vertx: Vertx, absoluteMounting: Boolean = false) {
  val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")
  val backendV1Router: Router = Router.router(vertx)
  val eventBus: EventBus = vertx.eventBus()

  backendV1Router.route().handler(BodyHandler.create())

  backendV1Router.post("/getBlockData").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val pageName = body.getString("page_name")
    val productId = body.getInteger("product_id")
    val token: String = ctx.request().headers()["Authorization"].replace("Bearer ", "")
//    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
//      if (it.getBoolean("success")) {
//        ctx.end()
//      } else {
//        ctx.response().setStatusCode(403).end("User does not have the permission for this")
//      }
//    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
//      if (it.getBoolean("success")) {
      eventBus.request<MutableList<FullBlockInfo>>("process.backend_block.getAllFullInfoByBlockNames", pageName, listDeliveryOptions).onFailure {
        println("Failed to get block info")
        ctx.end("Failed to get block info")
      }.onComplete {
        eventBus.request<JsonObject>("process.completeEav.getById", productId).onFailure {
          println("Failed to get product info")
          ctx.end("Failed to get product info")
        }.onComplete { product ->
          val fullProduct = product.result().body()
          val jsonElement = JsonParser.parseString(fullProduct.toString()).asJsonObject
          val blocks = it.result().body()
          val jsonResponse = JsonObject()
          blocks.forEach { block ->
            val blockInformationJson = JsonObject()
            val blockAttributesList = mutableListOf<JsonObject>()

            block.blockAttributes.forEach { blockAttribute ->
              if (blockAttribute.attributeType != "list" && blockAttribute.attributeName != "images") {
                val attributeJson = JsonObject()
                attributeJson.put("attribute_id", blockAttribute.attributeId)
                attributeJson.put("attribute_name", blockAttribute.attributeName)
                attributeJson.put("attribute_type", blockAttribute.attributeType)
                attributeJson.put(
                  "current_attribute_value",
                  jsonElement.get(blockAttribute.attributeId).convertJsonElement()

                )
                blockAttributesList.add(attributeJson)
              }
            }

            blockInformationJson.put("block_type", block.backendBlock.blockType)
            if (block.backendBlock.blockName == "Images") {
              blockInformationJson.put(
                "images",
                jsonElement.findValueByFieldName("images").convertJsonElement()
              )
            } else {
              blockInformationJson.put("attributes", blockAttributesList)
            }

            block.eavAttributeList.forEach { eavAttributeList ->
              blockInformationJson.put(
                eavAttributeList.attributeKey,
                jsonElement.findValueByFieldName(eavAttributeList.attributeKey).convertJsonElement()
              )
            }

            jsonResponse.put(block.backendBlock.blockName, blockInformationJson)
          }

          ctx.end(jsonResponse.toString())
        }
      }
//      } else {
//        ctx.end(it.getString("message"))
//      }
//    }
    }

  backendV1Router["/test"].handler { ctx ->
    eventBus.request<String>("process.completeEav.getById", 1).onFailure {
      println("Failed to get completeEav data")
      ctx.end("Failed to get completeEav data")
    }.onComplete {
      ctx.end(it.result().body())
    }
  }

  // TODO: routing
  backendV1Router.initImage(vertx)

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1*" else "/v1*"
  ).subRouter(backendV1Router)
}
