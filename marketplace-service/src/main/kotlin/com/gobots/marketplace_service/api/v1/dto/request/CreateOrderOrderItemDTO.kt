package com.gobots.marketplace_service.api.v1.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreateOrderOrderItemDTO(
    @field:NotBlank
    val productName: String,
    @field:Positive val quantity: Int,
    @field:Positive val unitPrice: BigDecimal)
