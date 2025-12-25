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
import org.springframework.amqp.rabbit.core.RabbitTemplate
import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import com.gobots.receiver_service.infrastructure.messaging.config.properties.RetryProperties
import com.gobots.receiver_service.infrastructure.messaging.config.properties.RoutingKeys
import java.time.Instant
import java.util.UUID

class OrderEventListenerTest {
    private val receivedEventService: ReceivedEventService = mockk(relaxed = true)
    private val rabbitTemplate: RabbitTemplate = mockk(relaxed = true)
    private val messagingProperties = MessagingProperties(
        exchange = "marketplace.orders.events",
        queue = "receiver.orders",
        routingKeys = RoutingKeys(
            all = "order.*",
            created = "order.created",
            paid = "order.paid",
            shipped = "order.shipped",
            completed = "order.completed",
            canceled = "order.canceled"
        ),
        retry = RetryProperties(
            exchange = "receiver.order.events.retry",
            queue1 = "receiver.order.events.retry.1",
            queue2 = "receiver.order.events.retry.2",
            delayMs1 = 5000,
            delayMs2 = 30000,
            routingKey = "order.retry"
        ),
        deadLetterExchange = "receiver.order.events.dlx",
        deadLetterQueue = "receiver.order.dlq",
        deadLetterRoutingKey = "receiver.order.events.dlq",
        deadLetterMaxRetries = 2,
        deadLetterRetryHeader = "x-retry-count"
    )
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val listener = OrderEventListener(
        receivedEventService,
        objectMapper,
        rabbitTemplate,
        messagingProperties
    )

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
        verify(exactly = 0) { rabbitTemplate.send(any<String>(), any<String>(), any()) }
    }

    @Test
    fun onMessage_whenServiceFails_schedulesRetryLevel1() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 2L,
            storeCode = "store_002",
            occurredAt = Instant.now()
        )

        val message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(payload)).build()

        every { receivedEventService.handle(any()) } throws RuntimeException("boom")

        listener.onMessage(message)

        verify(exactly = 1) {
            rabbitTemplate.send(
                messagingProperties.retry.exchange,
                messagingProperties.retry.queue1,
                any()
            )
        }
    }
}
