package com.gobots.marketplace_service.api.v1.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateOrderRequest(@field:NotBlank val storeCode: String,
                              @field:NotEmpty val items: List<CreateOrderOrderItemDTO> )
