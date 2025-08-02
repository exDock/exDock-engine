package com.ex_dock.ex_dock.database.sales

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

class SalesJdbcVerticle: VerticleBase() {

    private lateinit var client: MongoClient
    private lateinit var eventBus: EventBus

    companion object {
        const val FAILED = "failed"
        const val DELETED_SUCCESS = "Deleted successfully"
        const val ORDERS_CACHE_ADDRESS = "orders"
        const val INVOICES_CACHE_ADDRESS = "invoices"
        const val CREDIT_MEMOS_CACHE_ADDRESS = "credit_memos"
        const val TRANSACTIONS_CACHE_ADDRESS = "transactions"
        const val SHIPMENTS_CACHE_ADDRESS = "shipments"
    }

    private val orderDeliveryOptions = DeliveryOptions().setCodecName("OrderCodec")
    private val invoiceDeliveryOptions = DeliveryOptions().setCodecName("InvoiceCodec")
    private val creditMemoDeliveryOptions = DeliveryOptions().setCodecName("CreditMemoCodec")
    private val transactionDeliveryOptions = DeliveryOptions().setCodecName("TransactionCodec")
    private val shipmentDeliveryOptions = DeliveryOptions().setCodecName("ShipmentCodec")

    override fun start(): Future<*>? {
        client = vertx.getConnection()
        eventBus = vertx.eventBus()

        // Order
        getAllOrders()
        getOrderById()
        createOrder()
        updateOrder()
        deleteOrder()

        // Invoice
        getAllInvoices()
        getInvoiceById()
        createInvoice()
        updateInvoice()
        deleteInvoice()

        // CreditMemo
        getAllCreditMemos()
        getCreditMemoById()
        createCreditMemo()
        updateCreditMemo()
        deleteCreditMemo()

        // Transaction
        getAllTransactions()
        getTransactionById()
        createTransaction()
        updateTransaction()
        deleteTransaction()

        // Shipment
        getAllShipments()
        getShipmentById()
        createShipment()
        updateShipment()
        deleteShipment()

        return Future.succeededFuture<Unit>()
    }

    private fun getAllOrders() {
        val allOrderDataConsumer = eventBus.consumer<String>("process.sales.getAllOrders")
        allOrderDataConsumer.handler { message ->
            val query = JsonObject()
            client.find("orders", query).replyListMessage(message)
        }
    }

    private fun getOrderById() {
        val getOrderByIdConsumer = eventBus.consumer<String>("process.sales.getOrderById")
        getOrderByIdConsumer.handler { message ->
            val orderId = message.body()
            val query = JsonObject()
                .put("_id", orderId)
            client.find("orders", query).replySingleMessage(message)
        }
    }

    private fun createOrder() {
        val createOrderConsumer = eventBus.consumer<Order>("process.sales.createOrder")
        createOrderConsumer.handler { message ->
            val order = message.body()
            val document = order.toDocument()

            val rowsFuture = client.save("orders", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    order.orderId = lastInsertID
                }

                setCacheFlag(eventBus, ORDERS_CACHE_ADDRESS)
                message.reply(order, orderDeliveryOptions)
            }
        }
    }

    private fun updateOrder() {
        val updateOrderConsumer = eventBus.consumer<Order>("process.sales.updateOrder")
        updateOrderConsumer.handler { message ->
            val body = message.body()

            if (body.orderId == null) {
                message.fail(400, FAILED)
                return@handler
            }

            val document = body.toDocument()
            val rowsFuture = client.save("orders", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    body.orderId = lastInsertID
                }

                setCacheFlag(eventBus, ORDERS_CACHE_ADDRESS)
                message.reply(body, orderDeliveryOptions)
            }
        }
    }

    private fun deleteOrder() {
        val deleteOrderConsumer = eventBus.consumer<String>("process.sales.deleteOrder")
        deleteOrderConsumer.handler { message ->
            val orderId = message.body()
            val query = JsonObject()
                .put("_id", orderId)
            val rowsFuture = client.removeDocument("orders", query)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess {
                setCacheFlag(eventBus, ORDERS_CACHE_ADDRESS)
                message.reply(DELETED_SUCCESS)
            }
        }
    }

    private fun getAllInvoices() {
        val allInvoiceDataConsumer = eventBus.consumer<String>("process.sales.getAllInvoices")
        allInvoiceDataConsumer.handler { message ->
            val query = JsonObject()
            client.find("invoices", query).replyListMessage(message)
        }
    }

    private fun getInvoiceById() {
        val getInvoiceByIdConsumer = eventBus.consumer<String>("process.sales.getInvoiceById")
        getInvoiceByIdConsumer.handler { message ->
            val invoiceId = message.body()
            val query = JsonObject()
                .put("_id", invoiceId)
            client.find("invoices", query).replySingleMessage(message)
        }
    }

    private fun createInvoice() {
        val createInvoiceConsumer = eventBus.consumer<Invoice>("process.sales.createInvoice")
        createInvoiceConsumer.handler { message ->
            val invoice = message.body()
            val document = invoice.toDocument()

            val rowsFuture = client.save("invoices", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    invoice.invoiceId = lastInsertID
                }

                setCacheFlag(eventBus, INVOICES_CACHE_ADDRESS)
                message.reply(invoice, invoiceDeliveryOptions)
            }
        }
    }

    private fun updateInvoice() {
        val updateInvoiceConsumer = eventBus.consumer<Invoice>("process.sales.updateInvoice")
        updateInvoiceConsumer.handler { message ->
            val body = message.body()

            if (body.invoiceId == null) {
                message.fail(400, FAILED)
                return@handler
            }

            val document = body.toDocument()
            val rowsFuture = client.save("invoices", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    body.invoiceId = lastInsertID
                }

                setCacheFlag(eventBus, INVOICES_CACHE_ADDRESS)
                message.reply(body, invoiceDeliveryOptions)
            }
        }
    }

    private fun deleteInvoice() {
        val deleteInvoiceConsumer = eventBus.consumer<String>("process.sales.deleteInvoice")
        deleteInvoiceConsumer.handler { message ->
            val invoiceId = message.body()
            val query = JsonObject()
                .put("_id", invoiceId)
            val rowsFuture = client.removeDocument("invoices", query)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess {
                setCacheFlag(eventBus, INVOICES_CACHE_ADDRESS)
                message.reply(DELETED_SUCCESS)
            }
        }
    }

    private fun getAllCreditMemos() {
        val allCreditMemoDataConsumer = eventBus.consumer<String>("process.sales.getAllCreditMemos")
        allCreditMemoDataConsumer.handler { message ->
            val query = JsonObject()
            client.find("credit_memos", query).replyListMessage(message)
        }
    }

    private fun getCreditMemoById() {
        val getCreditMemoByIdConsumer = eventBus.consumer<String>("process.sales.getCreditMemoById")
        getCreditMemoByIdConsumer.handler { message ->
            val creditMemoId = message.body()
            val query = JsonObject()
                .put("_id", creditMemoId)
            client.find("credit_memos", query).replySingleMessage(message)
        }
    }

    private fun createCreditMemo() {
        val createCreditMemoConsumer = eventBus.consumer<CreditMemo>("process.sales.createCreditMemo")
        createCreditMemoConsumer.handler { message ->
            val creditMemo = message.body()
            val document = creditMemo.toDocument()

            val rowsFuture = client.save("credit_memos", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    creditMemo.creditMemoId = lastInsertID
                }

                setCacheFlag(eventBus, CREDIT_MEMOS_CACHE_ADDRESS)
                message.reply(creditMemo, creditMemoDeliveryOptions)
            }
        }
    }

    private fun updateCreditMemo() {
        val updateCreditMemoConsumer = eventBus.consumer<CreditMemo>("process.sales.updateCreditMemo")
        updateCreditMemoConsumer.handler { message ->
            val body = message.body()

            if (body.creditMemoId == null) {
                message.fail(400, FAILED)
                return@handler
            }

            val document = body.toDocument()
            val rowsFuture = client.save("credit_memos", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    body.creditMemoId = lastInsertID
                }

                setCacheFlag(eventBus, CREDIT_MEMOS_CACHE_ADDRESS)
                message.reply(body, creditMemoDeliveryOptions)
            }
        }
    }

    private fun deleteCreditMemo() {
        val deleteCreditMemoConsumer = eventBus.consumer<String>("process.sales.deleteCreditMemo")
        deleteCreditMemoConsumer.handler { message ->
            val creditMemoId = message.body()
            val query = JsonObject()
                .put("_id", creditMemoId)
            val rowsFuture = client.removeDocument("credit_memos", query)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess {
                setCacheFlag(eventBus, CREDIT_MEMOS_CACHE_ADDRESS)
                message.reply(DELETED_SUCCESS)
            }
        }
    }

    private fun getAllTransactions() {
        val allTransactionDataConsumer = eventBus.consumer<String>("process.sales.getAllTransactions")
        allTransactionDataConsumer.handler { message ->
            val query = JsonObject()
            client.find("transactions", query).replyListMessage(message)
        }
    }

    private fun getTransactionById() {
        val getTransactionByIdConsumer = eventBus.consumer<String>("process.sales.getTransactionById")
        getTransactionByIdConsumer.handler { message ->
            val transactionId = message.body()
            val query = JsonObject()
                .put("_id", transactionId)
            client.find("transactions", query).replySingleMessage(message)
        }
    }

    private fun createTransaction() {
        val createTransactionConsumer = eventBus.consumer<Transaction>("process.sales.createTransaction")
        createTransactionConsumer.handler { message ->
            val transaction = message.body()
            val document = transaction.toDocument()

            val rowsFuture = client.save("transactions", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    transaction.transactionId = lastInsertID
                }

                setCacheFlag(eventBus, TRANSACTIONS_CACHE_ADDRESS)
                message.reply(transaction, transactionDeliveryOptions)
            }
        }
    }

    private fun updateTransaction() {
        val updateTransactionConsumer = eventBus.consumer<Transaction>("process.sales.updateTransaction")
        updateTransactionConsumer.handler { message ->
            val body = message.body()

            if (body.transactionId == null) {
                message.fail(400, FAILED)
                return@handler
            }

            val document = body.toDocument()
            val rowsFuture = client.save("transactions", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    body.transactionId = lastInsertID
                }

                setCacheFlag(eventBus, TRANSACTIONS_CACHE_ADDRESS)
                message.reply(body, transactionDeliveryOptions)
            }
        }
    }

    private fun deleteTransaction() {
        val deleteTransactionConsumer = eventBus.consumer<String>("process.sales.deleteTransaction")
        deleteTransactionConsumer.handler { message ->
            val transactionId = message.body()
            val query = JsonObject()
                .put("_id", transactionId)
            val rowsFuture = client.removeDocument("transactions", query)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess {
                setCacheFlag(eventBus, TRANSACTIONS_CACHE_ADDRESS)
                message.reply(DELETED_SUCCESS)
            }
        }
    }

    private fun getAllShipments() {
        val allShipmentDataConsumer = eventBus.consumer<String>("process.sales.getAllShipments")
        allShipmentDataConsumer.handler { message ->
            val query = JsonObject()
            client.find("shipments", query).replyListMessage(message)
        }
    }

    private fun getShipmentById() {
        val getShipmentByIdConsumer = eventBus.consumer<String>("process.sales.getShipmentById")
        getShipmentByIdConsumer.handler { message ->
            val shipmentId = message.body()
            val query = JsonObject()
                .put("_id", shipmentId)
            client.find("shipments", query).replySingleMessage(message)
        }
    }

    private fun createShipment() {
        val createShipmentConsumer = eventBus.consumer<Shipment>("process.sales.createShipment")
        createShipmentConsumer.handler { message ->
            val shipment = message.body()
            val document = shipment.toDocument()

            val rowsFuture = client.save("shipments", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    shipment.shipmentId = lastInsertID
                }

                setCacheFlag(eventBus, SHIPMENTS_CACHE_ADDRESS)
                message.reply(shipment, shipmentDeliveryOptions)
            }
        }
    }

    private fun updateShipment() {
        val updateShipmentConsumer = eventBus.consumer<Shipment>("process.sales.updateShipment")
        updateShipmentConsumer.handler { message ->
            val body = message.body()

            if (body.shipmentId == null) {
                message.fail(400, FAILED)
                return@handler
            }

            val document = body.toDocument()
            val rowsFuture = client.save("shipments", document)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess { res ->
                val lastInsertID: String? = res
                if (lastInsertID != null) {
                    body.shipmentId = lastInsertID
                }

                setCacheFlag(eventBus, SHIPMENTS_CACHE_ADDRESS)
                message.reply(body, shipmentDeliveryOptions)
            }
        }
    }

    private fun deleteShipment() {
        val deleteShipmentConsumer = eventBus.consumer<String>("process.sales.deleteShipment")
        deleteShipmentConsumer.handler { message ->
            val shipmentId = message.body()
            val query = JsonObject()
                .put("_id", shipmentId)
            val rowsFuture = client.removeDocument("shipments", query)

            rowsFuture.onFailure { res ->
                println("Failed to execute query: ${'$'}res")
                message.fail(400, FAILED)
            }

            rowsFuture.onSuccess {
                setCacheFlag(eventBus, SHIPMENTS_CACHE_ADDRESS)
                message.reply(DELETED_SUCCESS)
            }
        }
    }
}