package com.ex_dock.ex_dock.database.list

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class ListVerticle : VerticleBase() {
  private lateinit var eventBus: EventBus
  private val addressDictionary = mutableMapOf(
    Pair("product", "process.product.getAllProducts"),
    Pair("category", "process.category.getAllCategories"),
    Pair("credit_memo", "process.sales.getAllCreditMemos"),
    Pair("invoice", "process.sales.getAllInvoices"),
    Pair("order", "process.sales.getAllOrders"),
    Pair("shipment", "process.sales.getAllShipments"),
    Pair("transaction", "process.sales.getAllTransactions"),
  )

  override fun start(): Future<*>? {
    eventBus = vertx.eventBus()

    delegateListRequest()

    return super.start();
  }

  private fun delegateListRequest() {
    eventBus.consumer("process.list.delegateRequest") { message ->
      val listName = message.body()

      if (addressDictionary.containsKey(listName)) {
        val address = addressDictionary[listName]
        eventBus.request< List<JsonObject>>(address, "").onFailure {
          message.fail(500, it.localizedMessage)
        }.onSuccess {
          val reply = it.body()
          val jsonArray = JsonArray(reply)
          message.reply(jsonArray)
        }
      }
    }
  }

}
