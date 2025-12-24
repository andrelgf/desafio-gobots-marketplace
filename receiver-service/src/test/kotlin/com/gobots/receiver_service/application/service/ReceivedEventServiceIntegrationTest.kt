package com.gobots.receiver_service.application.service

import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.domain.model.Subscription
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import com.gobots.receiver_service.domain.repository.ReceivedEventRepository
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import com.gobots.receiver_service.port.out.MarketplaceServicePort
import com.gobots.receiver_service.support.AbstractIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReceivedEventServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var receivedEventService: ReceivedEventService

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
    fun handle_persistsPayloadAsJsonb() {
        subscriptionRepository.save(Subscription("STORE_010"))

        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_SHIPPED,
            orderId = 99L,
            storeCode = "store_010",
            occurredAt = Instant.now()
        )

        val snapshot = objectMapper.createObjectNode().put("id", payload.orderId)
        every { marketplaceServicePort.getOrder(payload.orderId) } returns snapshot

        receivedEventService.handle(payload)

        val stored = receivedEventRepository.findAll()
        assertEquals(1, stored.size)
        assertEquals("STORE_010", stored.first().storeCode)
        assertNotNull(stored.first().payload)
        assertEquals(payload.orderId, stored.first().payload?.get("orderId")?.asLong())

        val snapshots = orderSnapshotRepository.findAll()
        assertEquals(1, snapshots.size)
        assertEquals(payload.orderId, snapshots.first().orderId)
        assertNotNull(snapshots.first().snapshot)
    }
}
