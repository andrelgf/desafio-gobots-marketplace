package com.gobots.receiver_service.infrastructure.messaging.listener

import com.gobots.receiver_service.application.service.ReceivedEventService
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.MessageBuilder
import java.time.Instant
import java.util.UUID

class OrderEventListenerTest {
    private val receivedEventService: ReceivedEventService = mockk(relaxed = true)
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val listener = OrderEventListener(receivedEventService, objectMapper)

    @Test
    fun onMessage_delegatesToService() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 1L,
            storeCode = "store_001",
            occurredAt = Instant.now()
        )

        val message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(payload)).build()

        listener.onMessage(message)

        verify(exactly = 1) { receivedEventService.handle(match { it.eventId == payload.eventId }) }
    }
}
