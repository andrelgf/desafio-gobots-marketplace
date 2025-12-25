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
    fun retryExchange(): DirectExchange =
        DirectExchange(props.retry.exchange, true, false)

    @Bean
    fun retryQueueLevel1(): Queue =
        QueueBuilder.durable(props.retry.queue1)
            .ttl(props.retry.delayMs1.toInt())
            .deadLetterExchange(props.exchange)
            .deadLetterRoutingKey(props.retry.routingKey)
            .build()

    @Bean
    fun retryQueueLevel2(): Queue =
        QueueBuilder.durable(props.retry.queue2)
            .ttl(props.retry.delayMs2.toInt())
            .deadLetterExchange(props.exchange)
            .deadLetterRoutingKey(props.retry.routingKey)
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
    fun retryQueueLevel1Binding(
        retryExchange: DirectExchange,
        retryQueueLevel1: Queue
    ): Binding =
        BindingBuilder.bind(retryQueueLevel1)
            .to(retryExchange)
            .with(props.retry.queue1)

    @Bean
    fun retryQueueLevel2Binding(
        retryExchange: DirectExchange,
        retryQueueLevel2: Queue
    ): Binding =
        BindingBuilder.bind(retryQueueLevel2)
            .to(retryExchange)
            .with(props.retry.queue2)

    @Bean
    fun ordersAllBinding(
        ordersExchange: TopicExchange,
        ordersQueue: Queue
    ): Binding =
        BindingBuilder.bind(ordersQueue)
            .to(ordersExchange)
            .with(props.routingKeys.all)

    @Bean
    fun ordersRetryBinding(
        ordersExchange: TopicExchange,
        ordersQueue: Queue
    ): Binding =
        BindingBuilder.bind(ordersQueue)
            .to(ordersExchange)
            .with(props.retry.routingKey)
}
