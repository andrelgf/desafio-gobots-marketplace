package com.gobots.receiver_service.infrastructure.messaging.config.properties

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class RetryProperties(
    @field:NotBlank
    val exchange: String,
    @field:NotBlank
    val queue1: String,
    @field:NotBlank
    val queue2: String,
    @field:Min(1)
    val delayMs1: Long,
    @field:Min(1)
    val delayMs2: Long,
    @field:NotBlank
    val routingKey: String
)
