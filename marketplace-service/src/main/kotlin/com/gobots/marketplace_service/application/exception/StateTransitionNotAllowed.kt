package com.gobots.marketplace_service.application.exception

import com.gobots.marketplace_service.domain.model.OrderStatus

class StateTransitionNotAllowed(
    val orderId: Long,
    val current: OrderStatus,
    val target: OrderStatus
) : RuntimeException("Order $orderId cannot transition from $current to $target")
