package com.gobots.receiver_service.infrastructure.messaging.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class DlqReprocessListener(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val messagingProperties: MessagingProperties
) {
    @RabbitListener(
        queues = ["\${app.messaging.dead-letter-queue}"],
        containerFactory = "rabbitListenerContainerFactory"
    )
    fun onDlqMessage(message: Message) {
        val retryHeader = "x-retry-count"
        val retryCount = (message.messageProperties.headers[retryHeader] as? Number)?.toInt() ?: 0
        if (retryCount >= messagingProperties.deadLetterMaxRetries) {
            return
        }

        val payload = try {
            objectMapper.readValue(message.body, OrderEventPayload::class.java)
        } catch (ex: Exception) {
            return
        }

        val routingKey = routingKeyFor(payload.eventType)
        rabbitTemplate.convertAndSend(messagingProperties.exchange, routingKey, payload) { msg ->
            msg.messageProperties.headers[retryHeader] = retryCount + 1
            msg
        }
    }

    private fun routingKeyFor(eventType: OrderEventType): String = when (eventType) {
        OrderEventType.ORDER_CREATED -> messagingProperties.routingKeys.created
        OrderEventType.ORDER_PAID -> messagingProperties.routingKeys.paid
        OrderEventType.ORDER_SHIPPED -> messagingProperties.routingKeys.shipped
        OrderEventType.ORDER_COMPLETED -> messagingProperties.routingKeys.completed
        OrderEventType.ORDER_CANCELED -> messagingProperties.routingKeys.canceled
    }
}
