package com.ex_dock.ex_dock.database.sales

import io.vertx.core.json.JsonObject

data class Order(
  var orderId: String?,
  var language: String,
  var date: String,
  var customerId: String,
  var status: String,
  var items: List<String>
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): Order {
      val itemList = emptyList<String>().toMutableList()
      jsonObject.getJsonArray("items").forEach { item ->
        itemList.add(item.toString())
      }

      return Order(
        orderId = jsonObject.getString("_id"),
        language = jsonObject.getString("language"),
        date = jsonObject.getString("date"),
        customerId = jsonObject.getString("customer_id"),
        status = jsonObject.getString("status"),
        items = itemList
      )
    }
  }
}

fun Order.toDocument(): JsonObject {
  val itemList = emptyList<JsonObject>().toMutableList()
  items.forEach { item ->
    itemList.add(
      JsonObject()
        .put("item_id", item)
    )
  }

  return JsonObject()
    .put("_id", orderId)
    .put("language", language)
    .put("date", date)
    .put("customer_id", customerId)
    .put("status", status)
    .put("items", itemList)
}

data class Invoice(
  var invoiceId: String?,
  var invoiceDate: String,
  var orderDate: String,
  var orderId: String,
  var status: String,
  var amount: Double,
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): Invoice {
      return Invoice(
        invoiceId = jsonObject.getString("_id"),
        invoiceDate = jsonObject.getString("invoice_date"),
        orderDate = jsonObject.getString("order_date"),
        orderId = jsonObject.getString("order_id"),
        status = jsonObject.getString("status"),
        amount = jsonObject.getDouble("amount")
      )
    }
  }
}

fun Invoice.toDocument(): JsonObject {
  return JsonObject()
    .put("_id", invoiceId)
    .put("invoice_date", invoiceDate)
    .put("order_date", orderDate)
    .put("order_id", orderId)
    .put("status", status)
    .put("amount", amount)
}

data class CreditMemo(
  var creditMemoId: String?,
  var creditMemoDate: String,
  var orderDate: String,
  var orderId: String,
  var status: String,
  var amount: Double,
  var returnItems: Boolean,
  var items: List<String>
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): CreditMemo {
      val itemList = emptyList<String>().toMutableList()
      jsonObject.getJsonArray("items").forEach { item ->
        itemList.add(item.toString())
      }

      return CreditMemo(
        creditMemoId = jsonObject.getString("_id"),
        creditMemoDate = jsonObject.getString("credit_memo_date"),
        orderDate = jsonObject.getString("order_date"),
        orderId = jsonObject.getString("order_id"),
        status = jsonObject.getString("status"),
        amount = jsonObject.getDouble("amount"),
        returnItems = jsonObject.getBoolean("return_items"),
        items = itemList
      )
    }
  }
}

fun CreditMemo.toDocument(): JsonObject {
  val itemList = emptyList<JsonObject>().toMutableList()
  items.forEach { item ->
    itemList.add(
      JsonObject()
        .put("item_id", item)
    )
  }

  return JsonObject()
    .put("_id", creditMemoId)
    .put("credit_memo_date", creditMemoDate)
    .put("order_date", orderDate)
    .put("order_id", orderId)
    .put("status", status)
    .put("amount", amount)
    .put("return_items", returnItems)
    .put("items", itemList)
}

data class Transaction(
  var transactionId: String?,
  var transactionDate: String,
  var orderId: String,
  var type: String,
  var method: String,
  var isClosed: Boolean,
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): Transaction {
      return Transaction(
        transactionId = jsonObject.getString("_id"),
        transactionDate = jsonObject.getString("transaction_date"),
        orderId = jsonObject.getString("order_id"),
        type = jsonObject.getString("type"),
        method = jsonObject.getString("method"),
        isClosed = jsonObject.getBoolean("is_closed")
      )
    }
  }
}

fun Transaction.toDocument(): JsonObject {
  return JsonObject()
    .put("_id", transactionId)
    .put("transaction_date", transactionDate)
    .put("order_id", orderId)
    .put("type", type)
    .put("method", method)
    .put("is_closed", isClosed)
}

data class Shipment(
  var shipmentId: String?,
  var shipmentDate: String,
  var address: String,
  var trackingNumber: String?,
  var orderId: String,
  var status: String,
  var items: List<String>,
) {
  companion object {
    fun fromJson(jsonObject: JsonObject): Shipment {
      val itemList = emptyList<String>().toMutableList()
      jsonObject.getJsonArray("items").forEach { item ->
        itemList.add(item.toString())
      }

      return Shipment(
        shipmentId = jsonObject.getString("_id"),
        shipmentDate = jsonObject.getString("shipment_date"),
        address = jsonObject.getString("address"),
        trackingNumber = jsonObject.getString("tracking_number"),
        orderId = jsonObject.getString("order_id"),
        status = jsonObject.getString("status"),
        items = itemList
      )
    }
  }
}

fun Shipment.toDocument(): JsonObject {
  val itemList = emptyList<JsonObject>().toMutableList()
  items.forEach { item ->
    itemList.add(
      JsonObject()
        .put("item_id", item)
    )
  }

  return JsonObject()
    .put("_id", shipmentId)
    .put("shipment_date", shipmentDate)
    .put("address", address)
    .put("tracking_number", trackingNumber)
    .put("order_id", orderId)
    .put("status", status)
    .put("items", itemList)
}
