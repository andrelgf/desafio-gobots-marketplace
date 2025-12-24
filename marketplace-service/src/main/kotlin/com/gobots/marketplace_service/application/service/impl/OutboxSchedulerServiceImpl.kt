package com.gobots.marketplace_service.application.service.impl

import com.gobots.marketplace_service.application.service.OutboxSchedulerService
import com.gobots.marketplace_service.config.properties.OutboxSchedulerProperties
import com.gobots.marketplace_service.domain.model.OutboxStatus
import com.gobots.marketplace_service.domain.repository.OutboxEventRepository
import com.gobots.marketplace_service.infrastructure.messaging.publisher.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class OutboxSchedulerServiceImpl(
    private val outboxEventRepository: OutboxEventRepository,
    private val eventPublisher: EventPublisher,
    private val outboxSchedulerProperties: OutboxSchedulerProperties
) : OutboxSchedulerService {

    companion object {
        private val log = LoggerFactory.getLogger(OutboxSchedulerServiceImpl::class.java)
    }

    @Scheduled(fixedDelayString = "\${app.outbox.scheduler.fixedDelayMs}")
    @Transactional
    override fun pollAndPublish() {
        if (!outboxSchedulerProperties.enabled) {
            log.debug("Outbox scheduler disabled, skipping poll.")
            return
        }

        val batchSize = outboxSchedulerProperties.batchSize
        if (batchSize <= 0) {
            log.warn("Outbox scheduler batchSize is invalid ({}), skipping poll.", batchSize)
            return
        }

        val events = outboxEventRepository.findPendingForUpdate(batchSize)
        if (events.isEmpty()) {
            log.debug("Outbox scheduler found no pending events.")
            return
        }

        val now = Instant.now()
        log.info("Outbox scheduler publishing {} event(s).", events.size)
        events.forEach { event ->
            event.attempts += 1
            try {
                eventPublisher.publish(event)
                event.status = OutboxStatus.PUBLISHED
                event.publishedAt = now
                event.lastError = null
                log.debug("Outbox event published: id={} eventId={} type={}", event.id, event.eventId, event.eventType)
            } catch (ex: Exception) {
                event.lastError = ex.message
                if (event.attempts >= outboxSchedulerProperties.maxAttempts) {
                    event.status = OutboxStatus.FAILED
                    log.warn(
                        "Outbox event failed and exceeded max attempts: id={} eventId={} attempts={} error={}",
                        event.id,
                        event.eventId,
                        event.attempts,
                        ex.message
                    )
                } else {
                    log.warn(
                        "Outbox event publish failed: id={} eventId={} attempts={} error={}",
                        event.id,
                        event.eventId,
                        event.attempts,
                        ex.message
                    )
                }
            }
        }

        outboxEventRepository.saveAll(events)
    }
}
