package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.database.category.CategoryInfo
import com.ex_dock.ex_dock.database.product.ProductInfo
import com.ex_dock.ex_dock.database.sales.CreditMemo
import com.ex_dock.ex_dock.database.sales.Invoice
import com.ex_dock.ex_dock.database.sales.Order
import com.ex_dock.ex_dock.database.sales.Shipment
import com.ex_dock.ex_dock.database.sales.Transaction
import com.ex_dock.ex_dock.helper.ExDockCache

class DataAccessor(
  private val productCache: ExDockCache<ProductInfo>,
  private val categoryCache: ExDockCache<CategoryInfo>,
  private val creditMemoCache: ExDockCache<CreditMemo>,
  private val invoiceCache: ExDockCache<Invoice>,
  private val orderCache: ExDockCache<Order>,
  private val shipmentCache: ExDockCache<Shipment>,
  private val transactionCache: ExDockCache<Transaction>,
) {

  fun get(type: String, key: String): Any? {
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
