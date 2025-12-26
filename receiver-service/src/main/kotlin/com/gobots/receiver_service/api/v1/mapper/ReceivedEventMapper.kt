package com.gobots.receiver_service.api.v1.mapper

import com.gobots.receiver_service.api.v1.dto.response.ReceivedEventDTO
import com.gobots.receiver_service.domain.model.ReceivedEvent
import org.springframework.stereotype.Component

@Component
class ReceivedEventMapper {
    fun toDTO(event: ReceivedEvent): ReceivedEventDTO =
        ReceivedEventDTO(
            id = requireNotNull(event.id) { "ReceivedEvent id cannot be null" },
            eventId = event.eventId,
            eventType = event.eventType,
            orderId = event.orderId,
            storeCode = event.storeCode,
            createdAt = requireNotNull(event.createdAt) { "ReceivedEvent createdAt cannot be null" },
            payload = event.payload
        )
}
