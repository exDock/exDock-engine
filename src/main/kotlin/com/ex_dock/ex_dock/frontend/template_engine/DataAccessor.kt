package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.database.category.CategoryInfo
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.sales.CreditMemo
import com.ex_dock.ex_dock.database.sales.Invoice
import com.ex_dock.ex_dock.database.sales.Order
import com.ex_dock.ex_dock.database.sales.Shipment
import com.ex_dock.ex_dock.database.sales.Transaction
import com.ex_dock.ex_dock.helper.AsyncExDockCache
import com.ex_dock.ex_dock.helper.ExDockCache
import java.util.concurrent.CompletableFuture

class DataAccessor(
  private val productCache: AsyncExDockCache<ProductInfo>,
  private val categoryCache: AsyncExDockCache<CategoryInfo>,
  private val creditMemoCache: AsyncExDockCache<CreditMemo>,
  private val invoiceCache: AsyncExDockCache<Invoice>,
  private val orderCache: AsyncExDockCache<Order>,
  private val shipmentCache: AsyncExDockCache<Shipment>,
  private val transactionCache: AsyncExDockCache<Transaction>,
) {

  fun get(type: String, key: String): CompletableFuture<*>? {
    return when (type.lowercase()) {
      "product" -> productCache.getById(key)
      "category" -> categoryCache.getById(key)
      "credit_memo" -> creditMemoCache.getById(key)
      "invoice" -> invoiceCache.getById(key)
      "order" -> orderCache.getById(key)
      "shipment" -> shipmentCache.getById(key)
      "transaction" -> transactionCache.getById(key)
      else -> null
    }
  }
}
