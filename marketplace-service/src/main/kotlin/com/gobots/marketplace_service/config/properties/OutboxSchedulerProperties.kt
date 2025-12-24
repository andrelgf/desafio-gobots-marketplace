package com.gobots.marketplace_service.config.properties

import jakarta.validation.constraints.AssertTrue
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.outbox.scheduler")
data class OutboxSchedulerProperties(
    val enabled: Boolean,
    val fixedDelayMs: Int,
    val batchSize: Int,
    val maxAttempts: Int,
){
    @get:AssertTrue(message = "fixedDelayMs, batchSize and maxAttempts must be > 0 when scheduler is enabled")
    val isValid: Boolean
        get() = !enabled || (fixedDelayMs > 0 && batchSize > 0 && maxAttempts > 0)
}
