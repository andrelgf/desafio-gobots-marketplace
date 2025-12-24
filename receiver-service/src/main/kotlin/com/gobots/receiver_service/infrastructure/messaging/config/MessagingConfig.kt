package com.gobots.receiver_service.infrastructure.messaging.config

import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MessagingProperties::class)
class MessagingConfig(private val props: MessagingProperties) {

    @Bean
    fun ordersExchange(): TopicExchange =
        TopicExchange(props.exchange, true, false)

    @Bean
    fun deadLetterExchange(): DirectExchange =
        DirectExchange(props.deadLetterExchange, true, false)

    @Bean
    fun ordersQueue(): Queue =
        QueueBuilder.durable(props.queue)
            .deadLetterExchange(props.deadLetterExchange)
            .deadLetterRoutingKey(props.deadLetterRoutingKey)
            .build()

    @Bean
    fun deadLetterQueue(): Queue =
        QueueBuilder.durable(props.deadLetterQueue).build()

    @Bean
    fun deadLetterBinding(
        deadLetterExchange: DirectExchange,
        deadLetterQueue: Queue
    ): Binding =
        BindingBuilder.bind(deadLetterQueue)
            .to(deadLetterExchange)
            .with(props.deadLetterRoutingKey)

    @Bean
    fun ordersAllBinding(
        ordersExchange: TopicExchange,
        ordersQueue: Queue
    ): Binding =
        BindingBuilder.bind(ordersQueue)
            .to(ordersExchange)
            .with(props.routingKeys.all)
}
