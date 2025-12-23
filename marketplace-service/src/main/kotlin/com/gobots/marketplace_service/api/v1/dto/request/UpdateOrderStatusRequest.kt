package com.gobots.marketplace_service.api.v1.dto.request

import com.gobots.marketplace_service.domain.model.OrderStatus
import jakarta.validation.constraints.NotNull

data class UpdateOrderStatusRequest(val status: OrderStatus)
