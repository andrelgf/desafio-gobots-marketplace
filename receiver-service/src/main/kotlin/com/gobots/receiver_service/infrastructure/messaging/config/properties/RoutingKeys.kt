package com.gobots.receiver_service.infrastructure.messaging.config.properties

data class RoutingKeys(
    val all: String,
    val created: String,
    val paid: String,
    val shipped: String,
    val completed: String,
    val canceled: String
)
