package com.gobots.receiver_service.application.service

import com.gobots.receiver_service.domain.model.Subscription

interface SubscriptionService {
    fun subscribe(storeCodes: List<String>): List<Subscription>
}