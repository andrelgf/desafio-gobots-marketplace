package com.gobots.receiver_service.api.v1.dto.response

import com.fasterxml.jackson.databind.JsonNode
import com.gobots.receiver_service.domain.model.OrderEventType
import java.time.Instant
import java.util.UUID

data class ReceivedEventDTO(
    val id: Long,
    val eventId: UUID,
    val eventType: OrderEventType,
    val orderId: Long,
    val storeCode: String,
    val createdAt: Instant,
    val payload: JsonNode?
)
