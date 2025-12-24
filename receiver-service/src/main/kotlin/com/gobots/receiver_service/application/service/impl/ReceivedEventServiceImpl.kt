package com.gobots.receiver_service.application.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.application.service.ReceivedEventService
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderSnapshot
import com.gobots.receiver_service.domain.model.ReceivedEvent
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import com.gobots.receiver_service.domain.repository.ReceivedEventRepository
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import com.gobots.receiver_service.port.out.MarketplaceServicePort
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReceivedEventServiceImpl(
    private val receivedEventRepository: ReceivedEventRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val orderSnapshotRepository: OrderSnapshotRepository,
    private val marketplaceServicePort: MarketplaceServicePort,
    private val objectMapper: ObjectMapper
) : ReceivedEventService {
    private val logger = LoggerFactory.getLogger(ReceivedEventServiceImpl::class.java)

    @Transactional
    override fun handle(payload: OrderEventPayload) {
        val storeCode = payload.storeCode.trim().uppercase()
        if (!subscriptionRepository.existsByStoreCode(storeCode)) {
            logger.debug("Skipping event {} for unsubscribed store {}", payload.eventId, storeCode)
            return
        }

        if (receivedEventRepository.existsByEventId(payload.eventId)) {
            logger.debug("Duplicate event {}, ignoring", payload.eventId)
            return
        }

        logger.debug("Fetching order snapshot for orderId={}", payload.orderId)
        val snapshot = marketplaceServicePort.getOrder(payload.orderId)

        val event = ReceivedEvent(
            eventId = payload.eventId,
            eventType = payload.eventType,
            orderId = payload.orderId,
            storeCode = storeCode,
            payload = objectMapper.valueToTree(payload)
        )

        try {
            receivedEventRepository.save(event)
            orderSnapshotRepository.save(
                OrderSnapshot(
                    orderId = payload.orderId,
                    eventId = payload.eventId,
                    snapshot = snapshot
                )
            )
            logger.info(
                "Processed event {} type={} orderId={} storeCode={}",
                payload.eventId,
                payload.eventType,
                payload.orderId,
                storeCode
            )
        } catch (ex: DataIntegrityViolationException) {
            // Duplicate eventId, ignore for idempotency.
            logger.debug("Duplicate event {} detected on save, ignoring", payload.eventId)
        }
    }
}
