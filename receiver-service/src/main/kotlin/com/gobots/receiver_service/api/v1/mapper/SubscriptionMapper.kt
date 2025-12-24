package com.gobots.receiver_service.api.v1.mapper

import com.gobots.receiver_service.api.v1.dto.response.SubscriptionDTO
import com.gobots.receiver_service.domain.model.Subscription
import org.springframework.stereotype.Component

@Component
class SubscriptionMapper {
    fun toDTO(subscription: Subscription): SubscriptionDTO =
        SubscriptionDTO(
            id = subscription.id!!,
            storeCode = subscription.storeCode,
            createdAt = subscription.createdAt!!
        )
}
