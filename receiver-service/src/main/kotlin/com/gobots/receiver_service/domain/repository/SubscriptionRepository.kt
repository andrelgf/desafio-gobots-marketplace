package com.gobots.receiver_service.domain.repository

import com.gobots.receiver_service.domain.model.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface SubscriptionRepository: JpaRepository<Subscription, Long> {
    fun findAllByStoreCodeIn(storeCodes: Collection<String>): List<Subscription>
}
