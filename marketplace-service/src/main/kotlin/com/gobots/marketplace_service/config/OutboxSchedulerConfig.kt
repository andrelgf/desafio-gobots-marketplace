package com.gobots.marketplace_service.config

import com.gobots.marketplace_service.config.properties.OutboxSchedulerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableConfigurationProperties(OutboxSchedulerProperties::class)
@EnableScheduling
class OutboxSchedulerConfig {
}
