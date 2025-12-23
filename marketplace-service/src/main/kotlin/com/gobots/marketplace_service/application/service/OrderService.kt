package com.gobots.marketplace_service.application.service

import com.gobots.marketplace_service.domain.model.Order
import com.gobots.marketplace_service.domain.model.OrderItem
import com.gobots.marketplace_service.domain.model.OrderStatus

interface OrderService {
    fun createOrder(storeCode: String, items: List<OrderItem>): Order
    fun getById(id: Long): Order
    fun updateOrderStatusById(id: Long, newStatus: OrderStatus): Order
}
