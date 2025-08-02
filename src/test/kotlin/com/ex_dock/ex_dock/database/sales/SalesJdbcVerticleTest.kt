package com.ex_dock.ex_dock.database.sales

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.registerGenericCodec
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class SalesJdbcVerticleTest {
    private lateinit var eventBus: EventBus
    private val orderDeliveryOptions = DeliveryOptions().setCodecName("OrderCodec")
    private val invoiceDeliveryOptions = DeliveryOptions().setCodecName("InvoiceCodec")
    private val creditMemoDeliveryOptions = DeliveryOptions().setCodecName("CreditMemoCodec")
    private val transactionDeliveryOptions = DeliveryOptions().setCodecName("TransactionCodec")
    private val shipmentDeliveryOptions = DeliveryOptions().setCodecName("ShipmentCodec")

    private val testOrder = Order(
        orderId = "1234567",
        language = "en",
        date = "2025-08-02",
        customerId = "1234567",
        status = "pending",
        items = listOf("1", "2", "3")
    )

    private val testInvoice = Invoice(
        invoiceId = "1234567",
        invoiceDate = "2025-08-02",
        orderDate = "2025-08-02",
        orderId = "1234567",
        status = "pending",
        amount = 100.0
    )

    private val testCreditMemo = CreditMemo(
        creditMemoId = "1234567",
        creditMemoDate = "2025-08-02",
        orderDate = "2025-08-02",
        orderId = "1234567",
        status = "pending",
        amount = 100.0,
        returnItems = true,
        items = listOf("1", "2", "3")
    )

    private val testTransaction = Transaction(
        transactionId = "1234567",
        transactionDate = "2025-08-02",
        orderId = "1234567",
        type = "sale",
        method = "credit_card",
        isClosed = false
    )

    private val testShipment = Shipment(
        shipmentId = "1234567",
        shipmentDate = "2025-08-02",
        address = "123 Main St",
        trackingNumber = "1234567",
        orderId = "1234567",
        status = "pending",
        items = listOf("1", "2", "3")
    )

    @Test
    @DisplayName("Test the sales classes functions")
    fun testSalesClassesFunctions(vertx: Vertx, context: VertxTestContext) {
        val suite = TestSuite.create("testSalesClassesFunctions")

        suite.test("testOrderToJson") { testContext ->
            val result = testOrder.toDocument()
            testContext.assertEquals(testOrder.orderId, result.getString("_id"))
            testContext.assertEquals(testOrder.language, result.getString("language"))
        }.test("testOrderFromJson") { testContext ->
            val orderJson = testOrder.toDocument()
            val order = Order.fromJson(orderJson)
            testContext.assertEquals(testOrder.orderId, order.orderId)
            testContext.assertEquals(testOrder.language, order.language)
        }

        suite.run(vertx).handler { res ->
            if (res.succeeded()) {
                context.completeNow()
            } else {
                context.failNow(res.cause())
            }
        }
    }

    @BeforeEach
    fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus = vertx.eventBus()
        eventBus.registerGenericCodec(Order::class)
        eventBus.registerGenericCodec(Invoice::class)
        eventBus.registerGenericCodec(CreditMemo::class)
        eventBus.registerGenericCodec(Transaction::class)
        eventBus.registerGenericCodec(Shipment::class)
        eventBus.registerGenericCodec(List::class)

        deployWorkerVerticleHelper(
            vertx,
            SalesJdbcVerticle::class.java.name,
            1,
            1
        ).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess {
            val f1 = eventBus.request<Order>("process.sales.createOrder", testOrder, orderDeliveryOptions)
            val f2 = eventBus.request<Invoice>("process.sales.createInvoice", testInvoice, invoiceDeliveryOptions)
            val f3 = eventBus.request<CreditMemo>("process.sales.createCreditMemo", testCreditMemo, creditMemoDeliveryOptions)
            val f4 = eventBus.request<Transaction>("process.sales.createTransaction", testTransaction, transactionDeliveryOptions)
            val f5 = eventBus.request<Shipment>("process.sales.createShipment", testShipment, shipmentDeliveryOptions)

          Future.all(f1, f2, f3, f4, f5).onSuccess {
                vertxTestContext.completeNow()
            }.onFailure {
                vertxTestContext.failNow(it)
            }
        }
    }

    @AfterEach
    fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val f1 = eventBus.request<String>("process.sales.deleteOrder", testOrder.orderId)
        val f2 = eventBus.request<String>("process.sales.deleteInvoice", testInvoice.invoiceId)
        val f3 = eventBus.request<String>("process.sales.deleteCreditMemo", testCreditMemo.creditMemoId)
        val f4 = eventBus.request<String>("process.sales.deleteTransaction", testTransaction.transactionId)
        val f5 = eventBus.request<String>("process.sales.deleteShipment", testShipment.shipmentId)

        Future.all(f1, f2, f3, f4, f5).onSuccess {
            vertxTestContext.completeNow()
        }.onFailure {
            vertxTestContext.failNow(it)
        }
    }

    @Test
    @DisplayName("Test getting all orders from the database")
    fun testGetAllOrders(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<List<JsonObject>>("process.sales.getAllOrders", "").onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val orders = message.body()
            assertTrue(orders.isNotEmpty())
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting an order by id from the database")
    fun testGetOrderById(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<JsonObject>("process.sales.getOrderById", testOrder.orderId).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val order = message.body()
            assertEquals(testOrder.orderId, order.getString("_id"))
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test updating an order in the database")
    fun testUpdateOrder(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val updatedOrder = testOrder.copy(status = "shipped")
        eventBus.request<Order>("process.sales.updateOrder", updatedOrder, orderDeliveryOptions).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val order = message.body()
            assertEquals(updatedOrder.status, order.status)
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting all invoices from the database")
    fun testGetAllInvoices(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<List<JsonObject>>("process.sales.getAllInvoices", "").onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val invoices = message.body()
            assertTrue(invoices.isNotEmpty())
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting an invoice by id from the database")
    fun testGetInvoiceById(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<JsonObject>("process.sales.getInvoiceById", testInvoice.invoiceId).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val invoice = message.body()
            assertEquals(testInvoice.invoiceId, invoice.getString("_id"))
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test updating an invoice in the database")
    fun testUpdateInvoice(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val updatedInvoice = testInvoice.copy(status = "paid")
        eventBus.request<Invoice>("process.sales.updateInvoice", updatedInvoice, invoiceDeliveryOptions).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val invoice = message.body()
            assertEquals(updatedInvoice.status, invoice.status)
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting all credit memos from the database")
    fun testGetAllCreditMemos(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<List<JsonObject>>("process.sales.getAllCreditMemos", "").onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val creditMemos = message.body()
            assertTrue(creditMemos.isNotEmpty())
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting a credit memo by id from the database")
    fun testGetCreditMemoById(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<JsonObject>("process.sales.getCreditMemoById", testCreditMemo.creditMemoId).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val creditMemo = message.body()
            assertEquals(testCreditMemo.creditMemoId, creditMemo.getString("_id"))
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test updating a credit memo in the database")
    fun testUpdateCreditMemo(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val updatedCreditMemo = testCreditMemo.copy(status = "refunded")
        eventBus.request<CreditMemo>("process.sales.updateCreditMemo", updatedCreditMemo, creditMemoDeliveryOptions).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val creditMemo = message.body()
            assertEquals(updatedCreditMemo.status, creditMemo.status)
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting all transactions from the database")
    fun testGetAllTransactions(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<List<JsonObject>>("process.sales.getAllTransactions", "").onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val transactions = message.body()
            assertTrue(transactions.isNotEmpty())
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting a transaction by id from the database")
    fun testGetTransactionById(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<JsonObject>("process.sales.getTransactionById", testTransaction.transactionId).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val transaction = message.body()
            assertEquals(testTransaction.transactionId, transaction.getString("_id"))
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test updating a transaction in the database")
    fun testUpdateTransaction(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val updatedTransaction = testTransaction.copy(isClosed = true)
        eventBus.request<Transaction>("process.sales.updateTransaction", updatedTransaction, transactionDeliveryOptions).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val transaction = message.body()
            assertEquals(updatedTransaction.isClosed, transaction.isClosed)
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting all shipments from the database")
    fun testGetAllShipments(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<List<JsonObject>>("process.sales.getAllShipments", "").onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val shipments = message.body()
            assertTrue(shipments.isNotEmpty())
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test getting a shipment by id from the database")
    fun testGetShipmentById(vertx: Vertx, vertxTestContext: VertxTestContext) {
        eventBus.request<JsonObject>("process.sales.getShipmentById", testShipment.shipmentId).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val shipment = message.body()
            assertEquals(testShipment.shipmentId, shipment.getString("_id"))
            vertxTestContext.completeNow()
        }
    }

    @Test
    @DisplayName("Test updating a shipment in the database")
    fun testUpdateShipment(vertx: Vertx, vertxTestContext: VertxTestContext) {
        val updatedShipment = testShipment.copy(status = "delivered")
        eventBus.request<Shipment>("process.sales.updateShipment", updatedShipment, shipmentDeliveryOptions).onFailure {
            vertxTestContext.failNow(it)
        }.onSuccess { message ->
            val shipment = message.body()
            assertEquals(updatedShipment.status, shipment.status)
            vertxTestContext.completeNow()
        }
    }
}
