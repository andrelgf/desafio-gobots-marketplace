package com.gobots.receiver_service.infrastructure.messaging.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.application.service.ReceivedEventService
import com.gobots.receiver_service.domain.model.OrderEventPayload
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class OrderEventListener(
    private val receivedEventService: ReceivedEventService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(OrderEventListener::class.java)

    @RabbitListener(
        queues = ["\${app.messaging.queue}"],
        containerFactory = "rabbitListenerContainerFactory"
    )
    fun onMessage(message: Message) {
        val payload = try {
            val node = objectMapper.readTree(message.body)
            if (node.isTextual) {
                objectMapper.readValue(node.asText(), OrderEventPayload::class.java)
            } else {
                objectMapper.treeToValue(node, OrderEventPayload::class.java)
            }
        } catch (ex: Exception) {
            logger.warn("Failed to deserialize order event payload", ex)
            throw ex
        }
        receivedEventService.handle(payload)
    }
}
