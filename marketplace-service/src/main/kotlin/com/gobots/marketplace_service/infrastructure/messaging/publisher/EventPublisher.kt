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
    private val ordersMessagingProperties: OrdersMessagingProperties,
    private  val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper
) {
    public fun publish(outboxEvent: OutboxEvent) {
        val routingKey = getRoutingKey(outboxEvent)
        val payload = objectMapper.readValue<OrderEventPayload>(outboxEvent.payload)
        rabbitTemplate.convertAndSend(ordersMessagingProperties.exchange, routingKey, payload)
    }

    private fun getRoutingKey(outboxEvent: OutboxEvent): String {
        val routingKey = when (outboxEvent.eventType) {
            OrderEventType.ORDER_CREATED -> ordersMessagingProperties.routingKeys.created
            OrderEventType.ORDER_PAID -> ordersMessagingProperties.routingKeys.paid
            OrderEventType.ORDER_SHIPPED -> ordersMessagingProperties.routingKeys.shipped
            OrderEventType.ORDER_COMPLETED -> ordersMessagingProperties.routingKeys.completed
            OrderEventType.ORDER_CANCELED -> ordersMessagingProperties.routingKeys.canceled
        }
        return routingKey
    }
}