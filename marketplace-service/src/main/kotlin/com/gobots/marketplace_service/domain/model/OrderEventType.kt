package com.gobots.marketplace_service.domain.model

enum class OrderEventType {
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_SHIPPED,
    ORDER_COMPLETED,
    ORDER_CANCELED
}