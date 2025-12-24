package com.gobots.marketplace_service.infrastructure.messaging.config.properties

data class RoutingKeys(
    val created: String,
    val paid: String,
    val shipped: String,
    val completed: String,
    val canceled: String
)
