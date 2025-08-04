package com.ex_dock.ex_dock.backend.v1.router.sales

import com.ex_dock.ex_dock.database.sales.*
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

fun Router.initSalesRouter(vertx: Vertx) {
  val salesRouter = Router.router(vertx)
  val eventBus = vertx.eventBus()

  salesRouter.route().handler(BodyHandler.create())

  // Orders
  salesRouter.get("/orders").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.sales.getAllOrders", "").onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(JsonObject().put("orders", message.body()).encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.get("/orders/:id").handler { ctx ->
    val orderId = ctx.pathParam("id")
    eventBus.request<JsonObject>("process.sales.getOrderById", orderId).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.post("/orders").handler { ctx ->
    val order = Order.fromJson(ctx.body().asJsonObject())
    eventBus.request<Order>("process.sales.createOrder", order, DeliveryOptions().setCodecName("OrderCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.put("/orders").handler { ctx ->
    val order = Order.fromJson(ctx.body().asJsonObject())
    eventBus.request<Order>("process.sales.updateOrder", order, DeliveryOptions().setCodecName("OrderCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.delete("/orders/:id").handler { ctx ->
    val orderId = ctx.pathParam("id")
    eventBus.request<String>("process.sales.deleteOrder", orderId).onSuccess { message ->
      ctx.response().end(message.body())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  // Invoices
  salesRouter.get("/invoices").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.sales.getAllInvoices", "").onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(JsonObject().put("invoices", message.body()).encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.get("/invoices/:id").handler { ctx ->
    val invoiceId = ctx.pathParam("id")
    eventBus.request<JsonObject>("process.sales.getInvoiceById", invoiceId).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.post("/invoices").handler { ctx ->
    val invoice = Invoice.fromJson(ctx.body().asJsonObject())
    eventBus.request<Invoice>("process.sales.createInvoice", invoice, DeliveryOptions().setCodecName("InvoiceCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.put("/invoices").handler { ctx ->
    val invoice = Invoice.fromJson(ctx.body().asJsonObject())
    eventBus.request<Invoice>("process.sales.updateInvoice", invoice, DeliveryOptions().setCodecName("InvoiceCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.delete("/invoices/:id").handler { ctx ->
    val invoiceId = ctx.pathParam("id")
    eventBus.request<String>("process.sales.deleteInvoice", invoiceId).onSuccess { message ->
      ctx.response().end(message.body())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  // Credit Memos
  salesRouter.get("/credit-memos").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.sales.getAllCreditMemos", "").onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(JsonObject().put("credit_memos", message.body()).encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.get("/credit-memos/:id").handler { ctx ->
    val creditMemoId = ctx.pathParam("id")
    eventBus.request<JsonObject>("process.sales.getCreditMemoById", creditMemoId).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.post("/credit-memos").handler { ctx ->
    val creditMemo = CreditMemo.fromJson(ctx.body().asJsonObject())
    eventBus.request<CreditMemo>("process.sales.createCreditMemo", creditMemo, DeliveryOptions().setCodecName("CreditMemoCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.put("/credit-memos").handler { ctx ->
    val creditMemo = CreditMemo.fromJson(ctx.body().asJsonObject())
    eventBus.request<CreditMemo>("process.sales.updateCreditMemo", creditMemo, DeliveryOptions().setCodecName("CreditMemoCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.delete("/credit-memos/:id").handler { ctx ->
    val creditMemoId = ctx.pathParam("id")
    eventBus.request<String>("process.sales.deleteCreditMemo", creditMemoId).onSuccess { message ->
      ctx.response().end(message.body())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  // Transactions
  salesRouter.get("/transactions").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.sales.getAllTransactions", "").onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(JsonObject().put("transactions", message.body()).encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.get("/transactions/:id").handler { ctx ->
    val transactionId = ctx.pathParam("id")
    eventBus.request<JsonObject>("process.sales.getTransactionById", transactionId).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.post("/transactions").handler { ctx ->
    val transaction = Transaction.fromJson(ctx.body().asJsonObject())
    eventBus.request<Transaction>("process.sales.createTransaction", transaction, DeliveryOptions().setCodecName("TransactionCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.put("/transactions").handler { ctx ->
    val transaction = Transaction.fromJson(ctx.body().asJsonObject())
    eventBus.request<Transaction>("process.sales.updateTransaction", transaction, DeliveryOptions().setCodecName("TransactionCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.delete("/transactions/:id").handler { ctx ->
    val transactionId = ctx.pathParam("id")
    eventBus.request<String>("process.sales.deleteTransaction", transactionId).onSuccess { message ->
      ctx.response().end(message.body())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  // Shipments
  salesRouter.get("/shipments").handler { ctx ->
    eventBus.request<List<JsonObject>>("process.sales.getAllShipments", "").onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(JsonObject().put("shipments", message.body()).encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.get("/shipments/:id").handler { ctx ->
    val shipmentId = ctx.pathParam("id")
    eventBus.request<JsonObject>("process.sales.getShipmentById", shipmentId).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.post("/shipments").handler { ctx ->
    val shipment = Shipment.fromJson(ctx.body().asJsonObject())
    eventBus.request<Shipment>("process.sales.createShipment", shipment, DeliveryOptions().setCodecName("ShipmentCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.put("/shipments").handler { ctx ->
    val shipment = Shipment.fromJson(ctx.body().asJsonObject())
    eventBus.request<Shipment>("process.sales.updateShipment", shipment, DeliveryOptions().setCodecName("ShipmentCodec")).onSuccess { message ->
      ctx.response().putHeader("content-type", "application/json").end(message.body().toDocument().encode())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  salesRouter.delete("/shipments/:id").handler { ctx ->
    val shipmentId = ctx.pathParam("id")
    eventBus.request<String>("process.sales.deleteShipment", shipmentId).onSuccess { message ->
      ctx.response().end(message.body())
    }.onFailure { error ->
      ctx.response().setStatusCode(500).end(error.message)
    }
  }

  this.route("/sales*").subRouter(salesRouter)
}
