package com.gobots.marketplace_service.application.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.marketplace_service.application.service.OutboxService
import com.gobots.marketplace_service.domain.model.OrderEventPayload
import com.gobots.marketplace_service.domain.model.OrderEventType
import com.gobots.marketplace_service.domain.model.OrderStatus
import com.gobots.marketplace_service.domain.model.OutboxEvents
import com.gobots.marketplace_service.domain.model.OutboxStatus
import com.gobots.marketplace_service.domain.repository.OutboxEventsRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class OutboxServiceImpl(
    private val outboxEventsRepository: OutboxEventsRepository,
    private val objectMapper: ObjectMapper,
) : OutboxService {

    override fun enqueue(orderId: Long, storeCode: String, newStatus: OrderStatus): UUID {
        val eventId = UUID.randomUUID()
        val occurredAt = Instant.now()

        val eventType = when (newStatus) {
            OrderStatus.CREATED -> OrderEventType.ORDER_CREATED
            OrderStatus.PAID -> OrderEventType.ORDER_PAID
            OrderStatus.SHIPPED -> OrderEventType.ORDER_SHIPPED
            OrderStatus.COMPLETED -> OrderEventType.ORDER_COMPLETED
            OrderStatus.CANCELED -> OrderEventType.ORDER_CANCELED
        }

        val payload = OrderEventPayload(
            eventId = eventId,
            eventType = eventType,
            orderId = orderId,
            storeCode = storeCode,
            occurredAt = occurredAt
        )

        val outbox = OutboxEvents(
            eventId = eventId,
            eventType = eventType,
            orderId = orderId,
            storeCode = storeCode,
            status = OutboxStatus.PENDING,
            attempts = 0,
            lastError = null,
            publishedAt = null,

            payload = objectMapper.writeValueAsString(payload)
        )

        outboxEventsRepository.save(outbox)
        return eventId
    }
}