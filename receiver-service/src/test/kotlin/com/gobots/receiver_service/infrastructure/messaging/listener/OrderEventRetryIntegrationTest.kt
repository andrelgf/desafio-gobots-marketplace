package com.gobots.receiver_service.infrastructure.messaging.listener

import com.gobots.receiver_service.application.service.ReceivedEventService
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import com.gobots.receiver_service.support.AbstractIntegrationTest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class OrderEventRetryIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var messagingProperties: MessagingProperties

    @MockkBean
    private lateinit var receivedEventService: ReceivedEventService

    @BeforeEach
    fun setup() {
        purgeQueue(messagingProperties.queue)
        purgeQueue(messagingProperties.retry.queue1)
        purgeQueue(messagingProperties.retry.queue2)
        purgeQueue(messagingProperties.deadLetterQueue)
    }

    @Test
    fun whenProcessingFails_messageIsScheduledForRetryLevel1() {
        every { receivedEventService.handle(any()) } throws RuntimeException("boom")

        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 1L,
            storeCode = "store_001",
            occurredAt = Instant.now()
        )

        rabbitTemplate.convertAndSend(
            messagingProperties.exchange,
            messagingProperties.routingKeys.created,
            payload
        )

        var retryMessage: Message? = null
        await.atMost(Duration.ofSeconds(5)).until {
            retryMessage = rabbitTemplate.receive(messagingProperties.retry.queue1)
            retryMessage != null
        }

        val retryHeader = messagingProperties.deadLetterRetryHeader
        val retryCount = (retryMessage!!.messageProperties.headers[retryHeader] as? Number)?.toInt()
        assertEquals(1, retryCount)
    }

    private fun purgeQueue(queueName: String) {
        rabbitTemplate.execute { channel ->
            channel.queuePurge(queueName)
            null
        }
    }
}
