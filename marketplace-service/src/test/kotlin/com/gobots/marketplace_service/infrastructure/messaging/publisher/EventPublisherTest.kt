package com.gobots.marketplace_service.infrastructure.messaging.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gobots.marketplace_service.domain.model.OrderEventPayload
import com.gobots.marketplace_service.domain.model.OrderEventType
import com.gobots.marketplace_service.domain.model.OutboxEvent
import com.gobots.marketplace_service.domain.model.OutboxStatus
import com.gobots.marketplace_service.infrastructure.messaging.config.properties.OrdersMessagingProperties
import com.gobots.marketplace_service.infrastructure.messaging.config.properties.RoutingKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventPublisherTest {
    private val rabbitTemplate: RabbitTemplate = mockk(relaxed = true)
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val props = OrdersMessagingProperties(
        exchange = "marketplace.orders.events",
        routingKeys = RoutingKeys(
            created = "order.created",
            paid = "order.paid",
            shipped = "order.shipped",
            completed = "order.completed",
            canceled = "order.canceled"
        )
    )
    private val publisher = EventPublisher(props, rabbitTemplate, objectMapper)

    @Test
    fun publish_sendsPayloadAsObjectAndSetsHeaders() {
        val eventId = UUID.randomUUID()
        val payload = OrderEventPayload(
            eventId = eventId,
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 123L,
            storeCode = "STORE_001",
            occurredAt = Instant.now()
        )

        val outboxEvent = OutboxEvent(
            eventId = eventId,
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 123L,
            storeCode = "STORE_001",
            status = OutboxStatus.PENDING,
            attempts = 0,
            payload = objectMapper.writeValueAsString(payload)
        )

        val messagePostProcessor = slot<org.springframework.amqp.core.MessagePostProcessor>()
        val bodySlot = slot<Any>()

        every {
            rabbitTemplate.convertAndSend(
                props.exchange,
                props.routingKeys.created,
                capture(bodySlot),
                capture(messagePostProcessor)
            )
        } returns Unit

        publisher.publish(outboxEvent)

        assertTrue(bodySlot.captured is OrderEventPayload)
        val capturedPayload = bodySlot.captured as OrderEventPayload
        assertEquals(eventId, capturedPayload.eventId)
        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(
                props.exchange,
                props.routingKeys.created,
                any(),
                any()
            )
        }
    }
}
