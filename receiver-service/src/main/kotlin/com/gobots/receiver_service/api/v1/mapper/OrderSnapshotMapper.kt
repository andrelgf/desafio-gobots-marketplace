package com.gobots.receiver_service.api.v1.mapper

import com.gobots.receiver_service.api.v1.dto.response.OrderSnapshotDTO
import com.gobots.receiver_service.domain.model.OrderSnapshot
import org.springframework.stereotype.Component

@Component
class OrderSnapshotMapper {
    fun toDTO(snapshot: OrderSnapshot): OrderSnapshotDTO =
        OrderSnapshotDTO(
            id = requireNotNull(snapshot.id) { "OrderSnapshot id cannot be null" },
            orderId = snapshot.orderId,
            eventId = snapshot.eventId,
            capturedAt = requireNotNull(snapshot.capturedAt) { "OrderSnapshot capturedAt cannot be null" },
            snapshot = snapshot.snapshot
        )
}
