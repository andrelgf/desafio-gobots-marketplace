package com.gobots.receiver_service.domain.model

import java.time.Instant
import java.util.UUID

data class OrderEventPayload(
    val eventId: UUID,
    val eventType: OrderEventType,
    val orderId: Long,
    val storeCode: String,
    val occurredAt: Instant
)
