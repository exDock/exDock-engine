package com.ex_dock.ex_dock.backend.v1.router.sales

import com.ex_dock.ex_dock.MainVerticle
import com.ex_dock.ex_dock.database.sales.*
import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.load
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.AfterEach
import java.util.*

@ExtendWith(VertxExtension::class)
class SalesRouterTest {
  var accessToken: String = ""
  var host = ""
  var port = 0

  private lateinit var testOrder: Order
  private lateinit var testInvoice: Invoice
  private lateinit var testCreditMemo: CreditMemo
  private lateinit var testTransaction: Transaction
  private lateinit var testShipment: Shipment

  private val orderDeliveryOptions = DeliveryOptions().setCodecName("OrderCodec")
  private val invoiceDeliveryOptions = DeliveryOptions().setCodecName("InvoiceCodec")
  private val creditMemoDeliveryOptions = DeliveryOptions().setCodecName("CreditMemoCodec")
  private val transactionDeliveryOptions = DeliveryOptions().setCodecName("TransactionCodec")
  private val shipmentDeliveryOptions = DeliveryOptions().setCodecName("ShipmentCodec")

  @BeforeEach
  @DisplayName("Deploying Server and creating test data")
  fun setup(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    vertx.eventBus().registerGenericCodec(Invoice::class)
    vertx.eventBus().registerGenericCodec(Order::class)
    vertx.eventBus().registerGenericCodec(CreditMemo::class)
    vertx.eventBus().registerGenericCodec(Shipment::class)
    vertx.eventBus().registerGenericCodec(Transaction::class)
    deployWorkerVerticleHelper(
      vertx,
      MainVerticle::class.qualifiedName.toString(),
      MainVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure {
      context.failNow(it)
    }.onSuccess {
      val props = Properties().load()
      val loginCredentials = JsonObject()
        .put("email", props.getProperty("ADMIN_EMAIL"))
        .put("password", props.getProperty("ADMIN_PASSWORD"))
      host = props.getProperty("HOST")
      port = props.getProperty("FRONTEND_PORT").toInt()


      client.request(HttpMethod.POST, port, host, "/api/v1/token").compose { request ->
        request.putHeader("Content-Type", "application/json")
        request.send(loginCredentials.encode()).compose(HttpClientResponse::body).onFailure {
          context.failNow(it)
        }.onSuccess { body ->
          val response = body.toJsonObject()
          val tokens = JsonObject(response.getString("tokens"))
          accessToken = tokens.getString("access_token")

          // Create test data
          testOrder = Order(orderId = UUID.randomUUID().toString(), language = "en", date = "2025-01-01", customerId = "cust1", status = "pending", items = listOf("item1"))
          testInvoice = Invoice(invoiceId = UUID.randomUUID().toString(), invoiceDate = "2025-01-01", orderDate = "2025-01-01", orderId = testOrder.orderId!!, status = "paid", amount = 100.0)
          testCreditMemo = CreditMemo(creditMemoId = UUID.randomUUID().toString(), creditMemoDate = "2025-01-01", orderDate = "2025-01-01", orderId = testOrder.orderId!!, status = "refunded", amount = 50.0, returnItems = true, items = listOf("item1"))
          testTransaction = Transaction(transactionId = UUID.randomUUID().toString(), transactionDate = "2025-01-01", orderId = testOrder.orderId!!, type = "payment", method = "credit_card", isClosed = false)
          testShipment = Shipment(shipmentId = UUID.randomUUID().toString(), shipmentDate = "2025-01-01", address = "123 Main St", trackingNumber = "track123", orderId = testOrder.orderId!!, status = "shipped", items = listOf("item1"))

          val f1 = vertx.eventBus().request<Order>("process.sales.createOrder", testOrder, orderDeliveryOptions)
          val f2 = vertx.eventBus().request<Invoice>("process.sales.createInvoice", testInvoice, invoiceDeliveryOptions)
          val f3 = vertx.eventBus().request<CreditMemo>("process.sales.createCreditMemo", testCreditMemo, creditMemoDeliveryOptions)
          val f4 = vertx.eventBus().request<Transaction>("process.sales.createTransaction", testTransaction, transactionDeliveryOptions)
          val f5 = vertx.eventBus().request<Shipment>("process.sales.createShipment", testShipment, shipmentDeliveryOptions)

          Future.all(f1, f2, f3, f4, f5).onSuccess {
            context.completeNow()
          }.onFailure {
            context.failNow(it)
          }
        }
      }
    }
  }

  @AfterEach
  @DisplayName("Deleting test data")
  fun tearDown(vertx: Vertx, context: VertxTestContext) {
    val f1 = vertx.eventBus().request<String>("process.sales.deleteOrder", testOrder.orderId)
    val f2 = vertx.eventBus().request<String>("process.sales.deleteInvoice", testInvoice.invoiceId)
    val f3 = vertx.eventBus().request<String>("process.sales.deleteCreditMemo", testCreditMemo.creditMemoId)
    val f4 = vertx.eventBus().request<String>("process.sales.deleteTransaction", testTransaction.transactionId)
    val f5 = vertx.eventBus().request<String>("process.sales.deleteShipment", testShipment.shipmentId)

    Future.all(f1, f2, f3, f4, f5).onSuccess {
      context.completeNow()
    }.onFailure {
      context.failNow(it)
    }
  }
  @Test
  @DisplayName("Test GET /sales/orders")
  fun testGetAllOrders(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/orders").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("orders"))
          assertTrue(jsonResponse.getJsonArray("orders").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /sales/orders/:id")
  fun testGetOrderById(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/orders/${testOrder.orderId}").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(testOrder.orderId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test POST /sales/orders")
  fun testCreateOrder(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val newOrder = Order(orderId = null, language = "es", date = "2025-01-02", customerId = "cust2", status = "processing", items = listOf("item2"))

    client.request(HttpMethod.POST, port, host, "/api/v1/sales/orders").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(newOrder.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertNotNull(jsonResponse.getString("_id"))
          // Clean up the created order
          vertx.eventBus().request<String>("process.sales.deleteOrder", jsonResponse.getString("_id")).onComplete {
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test PUT /sales/orders")
  fun testUpdateOrder(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val updatedOrder = testOrder.copy(status = "completed")

    client.request(HttpMethod.PUT, port, host, "/api/v1/sales/orders").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(updatedOrder.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(updatedOrder.orderId, jsonResponse.getString("_id"))
          assertEquals("completed", jsonResponse.getString("status"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test DELETE /sales/orders/:id")
  fun testDeleteOrder(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val orderIdToDelete = UUID.randomUUID().toString()
    val orderToDelete = Order(orderId = orderIdToDelete, language = "en", date = "2025-01-01", customerId = "cust1", status = "pending", items = listOf("item1"))

    vertx.eventBus().request<Order>("process.sales.createOrder", orderToDelete, orderDeliveryOptions).onFailure {
      context.failNow(it)
    }.onSuccess {
      client.request(HttpMethod.DELETE, port, host, "/api/v1/sales/orders/$orderIdToDelete").compose { request ->
        request.putHeader("Authorization", "Bearer $accessToken")
        request.send().onFailure {
          context.failNow(it)
        }.onSuccess { response ->
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Deleted successfully", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  // Invoices
  @Test
  @DisplayName("Test GET /sales/invoices")
  fun testGetAllInvoices(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/invoices").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("invoices"))
          assertTrue(jsonResponse.getJsonArray("invoices").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /sales/invoices/:id")
  fun testGetInvoiceById(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/invoices/${testInvoice.invoiceId}").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(testInvoice.invoiceId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test POST /sales/invoices")
  fun testCreateInvoice(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val newInvoice = Invoice(invoiceId = null, invoiceDate = "2025-01-02", orderDate = "2025-01-02", orderId = testOrder.orderId!!, status = "pending", amount = 120.0)

    client.request(HttpMethod.POST, port, host, "/api/v1/sales/invoices").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(newInvoice.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertNotNull(jsonResponse.getString("_id"))
          // Clean up the created invoice
          vertx.eventBus().request<String>("process.sales.deleteInvoice", jsonResponse.getString("_id")).onComplete {
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test PUT /sales/invoices")
  fun testUpdateInvoice(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val updatedInvoice = testInvoice.copy(status = "refunded")

    client.request(HttpMethod.PUT, port, host, "/api/v1/sales/invoices").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(updatedInvoice.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(updatedInvoice.invoiceId, jsonResponse.getString("_id"))
          assertEquals("refunded", jsonResponse.getString("status"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test DELETE /sales/invoices/:id")
  fun testDeleteInvoice(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val invoiceIdToDelete = UUID.randomUUID().toString()
    val invoiceToDelete = Invoice(invoiceId = invoiceIdToDelete, invoiceDate = "2025-01-01", orderDate = "2025-01-01", orderId = testOrder.orderId!!, status = "pending", amount = 100.0)

    vertx.eventBus().request<Invoice>("process.sales.createInvoice", invoiceToDelete, invoiceDeliveryOptions).onFailure {
      context.failNow(it)
    }.onSuccess {
      client.request(HttpMethod.DELETE, port, host, "/api/v1/sales/invoices/$invoiceIdToDelete").compose { request ->
        request.putHeader("Authorization", "Bearer $accessToken")
        request.send().onFailure {
          context.failNow(it)
        }.onSuccess { response ->
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Deleted successfully", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  // Credit Memos
  @Test
  @DisplayName("Test GET /sales/credit-memos")
  fun testGetAllCreditMemos(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/credit-memos").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("credit_memos"))
          assertTrue(jsonResponse.getJsonArray("credit_memos").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /sales/credit-memos/:id")
  fun testGetCreditMemoById(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/credit-memos/${testCreditMemo.creditMemoId}").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(testCreditMemo.creditMemoId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test POST /sales/credit-memos")
  fun testCreateCreditMemo(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val newCreditMemo = CreditMemo(creditMemoId = null, creditMemoDate = "2025-01-02", orderDate = "2025-01-02", orderId = testOrder.orderId!!, status = "pending", amount = 60.0, returnItems = false, items = listOf("item2"))

    client.request(HttpMethod.POST, port, host, "/api/v1/sales/credit-memos").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(newCreditMemo.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertNotNull(jsonResponse.getString("_id"))
          // Clean up the created credit memo
          vertx.eventBus().request<String>("process.sales.deleteCreditMemo", jsonResponse.getString("_id")).onComplete {
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test PUT /sales/credit-memos")
  fun testUpdateCreditMemo(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val updatedCreditMemo = testCreditMemo.copy(status = "completed")

    client.request(HttpMethod.PUT, port, host, "/api/v1/sales/credit-memos").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(updatedCreditMemo.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(updatedCreditMemo.creditMemoId, jsonResponse.getString("_id"))
          assertEquals("completed", jsonResponse.getString("status"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test DELETE /sales/credit-memos/:id")
  fun testDeleteCreditMemo(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val creditMemoIdToDelete = UUID.randomUUID().toString()
    val creditMemoToDelete = CreditMemo(creditMemoId = creditMemoIdToDelete, creditMemoDate = "2025-01-01", orderDate = "2025-01-01", orderId = testOrder.orderId!!, status = "refunded", amount = 50.0, returnItems = true, items = listOf("item1"))

    vertx.eventBus().request<CreditMemo>("process.sales.createCreditMemo", creditMemoToDelete, creditMemoDeliveryOptions).onFailure {
      context.failNow(it)
    }.onSuccess {
      client.request(HttpMethod.DELETE, port, host, "/api/v1/sales/credit-memos/$creditMemoIdToDelete").compose { request ->
        request.putHeader("Authorization", "Bearer $accessToken")
        request.send().onFailure {
          context.failNow(it)
        }.onSuccess { response ->
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Deleted successfully", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  // Transactions
  @Test
  @DisplayName("Test GET /sales/transactions")
  fun testGetAllTransactions(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/transactions").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("transactions"))
          assertTrue(jsonResponse.getJsonArray("transactions").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /sales/transactions/:id")
  fun testGetTransactionById(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/transactions/${testTransaction.transactionId}").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(testTransaction.transactionId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test POST /sales/transactions")
  fun testCreateTransaction(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val newTransaction = Transaction(transactionId = null, transactionDate = "2025-01-02", orderId = testOrder.orderId!!, type = "refund", method = "paypal", isClosed = true)

    client.request(HttpMethod.POST, port, host, "/api/v1/sales/transactions").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(newTransaction.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertNotNull(jsonResponse.getString("_id"))
          // Clean up the created transaction
          vertx.eventBus().request<String>("process.sales.deleteTransaction", jsonResponse.getString("_id")).onComplete {
            context.completeNow()
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Test PUT /sales/transactions")
  fun testUpdateTransaction(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val updatedTransaction = testTransaction.copy(isClosed = true)

    client.request(HttpMethod.PUT, port, host, "/api/v1/sales/transactions").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(updatedTransaction.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(updatedTransaction.transactionId, jsonResponse.getString("_id"))
          assertEquals(true, jsonResponse.getBoolean("is_closed"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test DELETE /sales/transactions/:id")
  fun testDeleteTransaction(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val transactionIdToDelete = UUID.randomUUID().toString()
    val transactionToDelete = Transaction(transactionId = transactionIdToDelete, transactionDate = "2025-01-01", orderId = testOrder.orderId!!, type = "payment", method = "credit_card", isClosed = false)

    vertx.eventBus().request<Transaction>("process.sales.createTransaction", transactionToDelete, transactionDeliveryOptions).onFailure {
      context.failNow(it)
    }.onSuccess {
      client.request(HttpMethod.DELETE, port, host, "/api/v1/sales/transactions/$transactionIdToDelete").compose { request ->
        request.putHeader("Authorization", "Bearer $accessToken")
        request.send().onFailure {
          context.failNow(it)
        }.onSuccess { response ->
          assertEquals(200, response.statusCode())
          response.body().onFailure {
            context.failNow(it)
          }.onSuccess { buffer ->
            assertEquals("Deleted successfully", buffer.toString())
            context.completeNow()
          }
        }
      }
    }
  }

  // Shipments
  @Test
  @DisplayName("Test GET /sales/shipments")
  fun testGetAllShipments(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/shipments").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertTrue(jsonResponse.containsKey("shipments"))
          assertTrue(jsonResponse.getJsonArray("shipments").size() >= 0)
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test GET /sales/shipments/:id")
  fun testGetShipmentById(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()

    client.request(HttpMethod.GET, port, host, "/api/v1/sales/shipments/${testShipment.shipmentId}").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(testShipment.shipmentId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test POST /sales/shipments")
  fun testCreateShipment(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val newShipment = Shipment(shipmentId = null, shipmentDate = "2025-01-01", address = "123 Main St", trackingNumber = "track123", orderId = "order1", status = "shipped", items = listOf("item1"))

    client.request(HttpMethod.POST, port, host, "/api/v1/sales/shipments").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(newShipment.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertNotNull(jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test PUT /sales/shipments")
  fun testUpdateShipment(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val updatedShipment = Shipment(shipmentId = "existingId", shipmentDate = "2025-01-01", address = "456 Oak Ave", trackingNumber = "track456", orderId = "order1", status = "delivered", items = listOf("item1"))

    client.request(HttpMethod.PUT, port, host, "/api/v1/sales/shipments").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.putHeader("Content-Type", "application/json")
      request.send(updatedShipment.toDocument().encode()).onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          val jsonResponse = buffer.toJsonObject()
          assertEquals(updatedShipment.shipmentId, jsonResponse.getString("_id"))
          context.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test DELETE /sales/shipments/:id")
  fun testDeleteShipment(vertx: Vertx, context: VertxTestContext) {
    val client = vertx.createHttpClient()
    val shipmentIdToDelete = "shipmentToDelete123"

    client.request(HttpMethod.DELETE, port, host, "/api/v1/sales/shipments/$shipmentIdToDelete").compose { request ->
      request.putHeader("Authorization", "Bearer $accessToken")
      request.send().onFailure {
        context.failNow(it)
      }.onSuccess { response ->
        assertEquals(200, response.statusCode())
        response.body().onFailure {
          context.failNow(it)
        }.onSuccess { buffer ->
          assertEquals("Deleted successfully", buffer.toString())
          context.completeNow()
        }
      }
    }
  }
}
