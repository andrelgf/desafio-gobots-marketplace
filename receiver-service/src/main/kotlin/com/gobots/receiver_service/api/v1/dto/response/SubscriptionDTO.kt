package com.gobots.receiver_service.api.v1.dto.response

import java.time.Instant

data class SubscriptionDTO(
    val id: Long,
    val storeCode: String,
    val createdAt: Instant
)
