package com.gobots.receiver_service.infrastructure.messaging.config.properties

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "app.messaging")
@Validated
data class MessagingProperties(
    @field:NotBlank
    val exchange: String,
    @field:NotBlank
    val queue: String,
    val routingKeys: RoutingKeys,
    @field:NotBlank
    val deadLetterExchange: String,
    @field:NotBlank
    val deadLetterQueue: String,
    @field:NotBlank
    val deadLetterRoutingKey: String,
    @field:Min(0)
    val deadLetterMaxRetries: Int = 3,
    @field:NotBlank
    val deadLetterRetryHeader: String = "x-retry-count"
)
