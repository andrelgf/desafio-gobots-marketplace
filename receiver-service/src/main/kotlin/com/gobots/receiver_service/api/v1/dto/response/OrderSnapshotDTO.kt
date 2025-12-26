package com.gobots.receiver_service.api.v1.dto.response

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.util.UUID

data class OrderSnapshotDTO(
    val id: Long,
    val orderId: Long,
    val eventId: UUID,
    val capturedAt: Instant,
    val snapshot: JsonNode?
)
