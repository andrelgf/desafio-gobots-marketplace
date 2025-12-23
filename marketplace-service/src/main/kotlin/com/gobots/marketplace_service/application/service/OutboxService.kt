package com.gobots.marketplace_service.application.service

import com.gobots.marketplace_service.domain.model.OrderStatus
import java.util.UUID

interface OutboxService {
    fun enqueue(orderId: Long, storeCode: String, newStatus: OrderStatus): UUID
}