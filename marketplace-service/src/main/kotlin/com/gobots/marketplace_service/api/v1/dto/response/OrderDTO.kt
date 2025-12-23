package com.gobots.marketplace_service.api.v1.dto.response

import com.gobots.marketplace_service.domain.model.OrderStatus
import java.math.BigDecimal
import java.time.Instant

data class OrderDTO(
    val id: Long,
    val status: OrderStatus,
    val storeCode: String,
    val items: List<OrderItemDTO>,
    val totalAmount: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)
