package com.gobots.marketplace_service.infrastructure.messaging.config

import com.gobots.marketplace_service.infrastructure.messaging.config.properties.OrdersMessagingProperties
import org.springframework.amqp.core.TopicExchange
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(OrdersMessagingProperties::class)
class MessagingConfig (private val properties: OrdersMessagingProperties){
    @Bean
    fun ordersExchange(): TopicExchange {
        return TopicExchange(properties.exchange)
    }
}
