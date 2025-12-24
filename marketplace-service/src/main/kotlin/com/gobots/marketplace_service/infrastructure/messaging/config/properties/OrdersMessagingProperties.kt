package com.gobots.marketplace_service.infrastructure.messaging.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.messaging")
data class OrdersMessagingProperties(
    val exchange: String,
    val routingKeys: RoutingKeys
)
