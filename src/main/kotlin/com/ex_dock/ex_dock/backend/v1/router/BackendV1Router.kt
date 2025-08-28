package com.ex_dock.ex_dock.backend.v1.router

import com.ex_dock.ex_dock.backend.apiMountingPath
import com.ex_dock.ex_dock.backend.v1.router.auth.AuthProvider
import com.ex_dock.ex_dock.backend.v1.router.file.initFileRouter
import com.ex_dock.ex_dock.frontend.auth.ExDockAuthHandler
import com.ex_dock.ex_dock.backend.v1.router.image.initImage
import com.ex_dock.ex_dock.backend.v1.router.system.enableSystemRouter
import com.ex_dock.ex_dock.backend.v1.router.template.initTemplateRouter
import com.ex_dock.ex_dock.database.backend_block.BlockInfo
import com.ex_dock.ex_dock.helper.convertJsonElement
import com.ex_dock.ex_dock.helper.findValueByFieldName
import com.ex_dock.ex_dock.helper.sendError
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
  val authProvider = AuthProvider()
  val exDockAuthHandler = ExDockAuthHandler(vertx)

  backendV1Router.route().handler(BodyHandler.create())

  backendV1Router.post("/getBlockData").handler { ctx ->
    val body = ctx.body().asJsonObject()
    val pageName = body.getString("page_name")
    val productId = body.getString("product_id")
    val token: String = ctx.request().headers()["Authorization"].replace("Bearer ", "")
//    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
//      if (it.getBoolean("success")) {
//        ctx.end()
//      } else {
//        ctx.response().setStatusCode(403).end("User does not have the permission for this")
//      }
//    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
//      if (it.getBoolean("success")) {
      eventBus.request<MutableList<JsonObject>>("process.backendBlock.getBackendBlocksByPageName", pageName, listDeliveryOptions).onFailure {
        println("Failed to get block info")
        ctx.end("Failed to get block info")
      }.onSuccess {
        eventBus.request<JsonObject>("process.product.getProductById", productId).onFailure {
          println("Failed to get product info")
          ctx.end("Failed to get product info")
        }.onSuccess { product ->
          val fullProduct = product.body()
          val jsonElement = JsonParser.parseString(fullProduct.toString()).asJsonObject
          val blocks = BlockInfo.fromJsonList(it.body())
          val jsonResponse = JsonObject()
          blocks.forEach { block ->
            val blockInformationJson = JsonObject()
            val blockAttributesList = mutableListOf<JsonObject>()

            block.blockAttributes.forEach { blockAttribute ->
              if (blockAttribute.attributeName != "images") {
                val attributeJson = JsonObject()
                attributeJson.put("attribute_id", blockAttribute.attributeId)
                attributeJson.put("attribute_name", blockAttribute.attributeName)
                attributeJson.put("attribute_type", blockAttribute.attributeType)
                attributeJson.put(
                  "current_attribute_value",
                  jsonElement.get(blockAttribute.attributeId.replace("product_", "")).convertJsonElement()

                )
                blockAttributesList.add(attributeJson)
              }
            }

            blockInformationJson.put("block_type", block.blockType)
            if (block.blockName == "Images") {
              blockInformationJson.put(
                "images",
                jsonElement.findValueByFieldName("images").convertJsonElement()
              )
            } else {
              blockInformationJson.put("attributes", blockAttributesList)
            }

            jsonResponse.put(block.blockName, blockInformationJson)
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
//    val token: String = ctx.request().headers()["Authorization"].replace("Bearer ", "")
//    exDockAuthHandler.verifyPermissionAuthorization(token, "userREAD") {
//      if (it.getBoolean("success")) {
//        ctx.end()
//      } else {
//        ctx.response().setStatusCode(403).end("User does not have the permission for this")
//      }
    vertx.eventBus().sendError(Exception("Test error to test websockets!"))
    ctx.end()
  }


  // TODO: routing
  backendV1Router.initImage(vertx)
  backendV1Router.enableSystemRouter(vertx)
  backendV1Router.initFileRouter(vertx)
  backendV1Router.initTemplateRouter(vertx)

  this.route(
    if (absoluteMounting) "$apiMountingPath/v1*" else "/v1*"
  ).subRouter(backendV1Router)
}
