package com.gobots.receiver_service.application.service.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.domain.model.OrderSnapshot
import com.gobots.receiver_service.domain.model.ReceivedEvent
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import com.gobots.receiver_service.domain.repository.ReceivedEventRepository
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import com.gobots.receiver_service.port.out.MarketplaceServicePort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class ReceivedEventServiceImplTest {
    private val receivedEventRepository: ReceivedEventRepository = mockk()
    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val orderSnapshotRepository: OrderSnapshotRepository = mockk()
    private val marketplaceServicePort: MarketplaceServicePort = mockk()
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val service = ReceivedEventServiceImpl(
        receivedEventRepository,
        subscriptionRepository,
        orderSnapshotRepository,
        marketplaceServicePort,
        objectMapper
    )

    @Test
    fun handle_whenStoreNotSubscribed_skipsProcessing() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 10L,
            storeCode = "store_001",
            occurredAt = Instant.now()
        )

        every { subscriptionRepository.existsByStoreCode("STORE_001") } returns false

        service.handle(payload)

        verify(exactly = 1) { subscriptionRepository.existsByStoreCode("STORE_001") }
        verify(exactly = 0) { receivedEventRepository.existsByEventId(any()) }
        verify(exactly = 0) { receivedEventRepository.save(any<ReceivedEvent>()) }
        verify(exactly = 0) { orderSnapshotRepository.save(any<OrderSnapshot>()) }
    }

    @Test
    fun handle_whenEventAlreadyProcessed_skipsSave() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 10L,
            storeCode = "store_001",
            occurredAt = Instant.now()
        )

        every { subscriptionRepository.existsByStoreCode("STORE_001") } returns true
        every { receivedEventRepository.existsByEventId(payload.eventId) } returns true

        service.handle(payload)

        verify(exactly = 1) { receivedEventRepository.existsByEventId(payload.eventId) }
        verify(exactly = 0) { receivedEventRepository.save(any<ReceivedEvent>()) }
        verify(exactly = 0) { orderSnapshotRepository.save(any<OrderSnapshot>()) }
    }

    @Test
    fun handle_whenNewEvent_savesNormalizedEvent() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_PAID,
            orderId = 20L,
            storeCode = " store_002 ",
            occurredAt = Instant.now()
        )

        val capturedEvent = slot<ReceivedEvent>()
        val capturedSnapshot = slot<OrderSnapshot>()
        val snapshotJson = objectMapper.createObjectNode().put("id", payload.orderId)
        every { subscriptionRepository.existsByStoreCode("STORE_002") } returns true
        every { receivedEventRepository.existsByEventId(payload.eventId) } returns false
        every { marketplaceServicePort.getOrder(payload.orderId) } returns snapshotJson
        every { receivedEventRepository.save(capture(capturedEvent)) } answers { capturedEvent.captured }
        every { orderSnapshotRepository.save(capture(capturedSnapshot)) } answers { capturedSnapshot.captured }

        service.handle(payload)

        assertEquals("STORE_002", capturedEvent.captured.storeCode)
        assertEquals(payload.eventId, capturedEvent.captured.eventId)
        assertEquals(capturedEvent.captured.payload?.has("eventId"), true)
        assertEquals(payload.eventId, capturedSnapshot.captured.eventId)
        assertEquals(payload.orderId, capturedSnapshot.captured.orderId)
        verify(exactly = 1) { receivedEventRepository.save(any<ReceivedEvent>()) }
        verify(exactly = 1) { orderSnapshotRepository.save(any<OrderSnapshot>()) }
    }

    @Test
    fun handle_whenDuplicateInsertHappens_doesNotThrow() {
        val payload = OrderEventPayload(
            eventId = UUID.randomUUID(),
            eventType = OrderEventType.ORDER_CREATED,
            orderId = 30L,
            storeCode = "store_003",
            occurredAt = Instant.now()
        )

        every { subscriptionRepository.existsByStoreCode("STORE_003") } returns true
        every { receivedEventRepository.existsByEventId(payload.eventId) } returns false
        every { marketplaceServicePort.getOrder(payload.orderId) } returns
            objectMapper.createObjectNode().put("id", payload.orderId)
        every { receivedEventRepository.save(any<ReceivedEvent>()) } throws DataIntegrityViolationException("dup")

        service.handle(payload)
    }
}
