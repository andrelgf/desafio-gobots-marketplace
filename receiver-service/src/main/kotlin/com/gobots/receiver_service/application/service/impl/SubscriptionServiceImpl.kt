package com.gobots.receiver_service.application.service.impl

import com.gobots.receiver_service.application.exception.InvalidSubscriptionRequestException
import com.gobots.receiver_service.application.service.SubscriptionService
import com.gobots.receiver_service.domain.model.Subscription
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionServiceImpl(private val subscriptionRepository: SubscriptionRepository): SubscriptionService {
    @Transactional
    override fun subscribe(storeCodes: List<String>): List<Subscription> {
        val normalized = storeCodes
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.uppercase() }
            .distinct()

        if (normalized.isEmpty()) {
            throw InvalidSubscriptionRequestException("storeIds must contain at least one non-blank value")
        }

        val existing = subscriptionRepository.findAllByStoreCodeIn(normalized)
            .map { it.storeCode }
            .toSet()

        val toCreate = normalized.filterNot { existing.contains(it) }
        if (toCreate.isEmpty()) {
            return emptyList()
        }

        val newSubscriptions = toCreate.map { Subscription(it) }
        return subscriptionRepository.saveAll(newSubscriptions)
    }

    @Transactional(readOnly = true)
    override fun listAll(): List<Subscription> {
        return subscriptionRepository.findAll()
    }
}
