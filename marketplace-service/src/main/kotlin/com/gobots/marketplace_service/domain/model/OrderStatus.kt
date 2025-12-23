package com.gobots.marketplace_service.domain.model

enum class OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    COMPLETED,
    CANCELED;

    fun canTransitionTo(target: OrderStatus): Boolean =
        when (this) {
            CREATED -> target == PAID || target == CANCELED
            PAID -> target == SHIPPED || target == CANCELED
            SHIPPED -> target == COMPLETED || target == CANCELED
            COMPLETED -> false
            CANCELED -> false
        }
}