package com.gobots.marketplace_service.infrastructure.messaging.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gobots.marketplace_service.domain.model.OrderEventPayload
import com.gobots.marketplace_service.domain.model.OrderEventType
import com.gobots.marketplace_service.domain.model.OutboxEvent
import com.gobots.marketplace_service.domain.model.OutboxStatus
import com.gobots.marketplace_service.infrastructure.messaging.config.properties.OrdersMessagingProperties
import com.gobots.marketplace_service.infrastructure.messaging.config.properties.RoutingKeys
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

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

        val messageSlot = slot<Message>()

        publisher.publish(outboxEvent)

        verify(exactly = 1) {
            rabbitTemplate.send(
                eq(props.exchange),
                eq(props.routingKeys.created),
                capture(messageSlot)
            )
        }

        val capturedPayload = objectMapper.readValue(messageSlot.captured.body, OrderEventPayload::class.java)
        assertEquals(eventId, capturedPayload.eventId)
        assertEquals("application/json", messageSlot.captured.messageProperties.contentType)
        assertEquals(eventId.toString(), messageSlot.captured.messageProperties.headers["x-event-id"])
        assertEquals(OrderEventType.ORDER_CREATED.name, messageSlot.captured.messageProperties.headers["x-event-type"])
    }
}
