package com.gobots.marketplace_service.api.v1.dto.response

import java.math.BigDecimal

data class OrderItemDTO(
    val id: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalAmount: BigDecimal
)