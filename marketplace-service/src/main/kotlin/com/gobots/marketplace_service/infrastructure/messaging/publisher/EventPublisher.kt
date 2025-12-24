package com.gobots.marketplace_service.infrastructure.messaging.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gobots.marketplace_service.domain.model.OrderEventPayload
import com.gobots.marketplace_service.domain.model.OrderEventType
import com.gobots.marketplace_service.domain.model.OutboxEvent
import com.gobots.marketplace_service.infrastructure.messaging.config.properties.OrdersMessagingProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val props: OrdersMessagingProperties,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper
) {
    fun publish(outboxEvent: OutboxEvent) {
        val routingKey = routingKey(outboxEvent.eventType)

        val payload = objectMapper.readValue(outboxEvent.payload, OrderEventPayload::class.java)

        rabbitTemplate.convertAndSend(props.exchange, routingKey, outboxEvent.payload) { msg ->
            val mp = msg.messageProperties
            mp.contentType = "application/json"
            mp.setHeader("x-event-id", payload.eventId.toString())
            mp.setHeader("x-event-type", payload.eventType.name)
            msg
        }
    }

    private fun routingKey(type: OrderEventType): String =
        when (type) {
            OrderEventType.ORDER_CREATED -> props.routingKeys.created
            OrderEventType.ORDER_PAID -> props.routingKeys.paid
            OrderEventType.ORDER_SHIPPED -> props.routingKeys.shipped
            OrderEventType.ORDER_COMPLETED -> props.routingKeys.completed
            OrderEventType.ORDER_CANCELED -> props.routingKeys.canceled
        }
}