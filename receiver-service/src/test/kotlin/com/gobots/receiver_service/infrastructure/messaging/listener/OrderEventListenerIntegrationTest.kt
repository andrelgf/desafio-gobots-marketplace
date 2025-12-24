package com.gobots.receiver_service.infrastructure.messaging.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.domain.model.Subscription
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import com.gobots.receiver_service.domain.repository.ReceivedEventRepository
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import com.gobots.receiver_service.port.out.MarketplaceServicePort
import com.gobots.receiver_service.support.AbstractIntegrationTest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderEventListenerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var messagingProperties: MessagingProperties

    @Autowired
    private lateinit var receivedEventRepository: ReceivedEventRepository

    @Autowired
    private lateinit var orderSnapshotRepository: OrderSnapshotRepository

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var marketplaceServicePort: MarketplaceServicePort

    @BeforeEach
    fun setup() {
        receivedEventRepository.deleteAll()
        orderSnapshotRepository.deleteAll()
        subscriptionRepository.deleteAll()
    }

    @Test
    fun publishAndConsume_persistsReceivedEvent() {
        subscriptionRepository.save(Subscription("STORE_900"))

        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 900L,
            storeCode = "store_900",
            occurredAt = Instant.now()
        )

        val snapshot = objectMapper.createObjectNode().put("id", payload.orderId)
        every { marketplaceServicePort.getOrder(payload.orderId) } returns snapshot

        rabbitTemplate.convertAndSend(
            messagingProperties.exchange,
            messagingProperties.routingKeys.created,
            payload
        )

        await.atMost(Duration.ofSeconds(5)).until { receivedEventRepository.count() > 0L }

        val stored = receivedEventRepository.findAll()
        assertEquals(1, stored.size)
        assertEquals("STORE_900", stored.first().storeCode)
        assertNotNull(stored.first().payload)

        val snapshots = orderSnapshotRepository.findAll()
        assertEquals(1, snapshots.size)
        assertEquals(payload.orderId, snapshots.first().orderId)
    }
}