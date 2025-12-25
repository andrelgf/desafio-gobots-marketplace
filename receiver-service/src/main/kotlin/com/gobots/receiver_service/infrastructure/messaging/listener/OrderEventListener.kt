package com.gobots.receiver_service.infrastructure.messaging.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.application.service.ReceivedEventService
import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.infrastructure.messaging.config.properties.MessagingProperties
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.core.MessageBuilder
import org.springframework.stereotype.Component

@Component
class OrderEventListener(
    private val receivedEventService: ReceivedEventService,
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate,
    private val messagingProperties: MessagingProperties
) {
    private val logger = LoggerFactory.getLogger(OrderEventListener::class.java)

    @RabbitListener(
        queues = ["\${app.messaging.queue}"],
        containerFactory = "rabbitListenerContainerFactory"
    )
    fun onMessage(message: Message) {
        val retryHeader = messagingProperties.deadLetterRetryHeader
        val retryCount = (message.messageProperties.headers[retryHeader] as? Number)?.toInt() ?: 0

        try {
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
        } catch (ex: Exception) {
            if (retryCount >= messagingProperties.deadLetterMaxRetries) {
                sendToDeadLetterQueue(message, retryHeader, retryCount)
                logger.warn("Sent message to DLQ after {} retries", retryCount)
                return
            }

            val nextQueue = if (retryCount == 0) {
                messagingProperties.retry.queue1
            } else {
                messagingProperties.retry.queue2
            }

            val retryMessage = MessageBuilder.fromMessage(message)
                .setHeader(retryHeader, retryCount + 1)
                .build()

            rabbitTemplate.send(messagingProperties.retry.exchange, nextQueue, retryMessage)
            logger.warn("Scheduled retry {} via {}", retryCount + 1, nextQueue, ex)
        }
    }

    private fun sendToDeadLetterQueue(message: Message, retryHeader: String, retryCount: Int) {
        val dlqMessage = MessageBuilder.fromMessage(message)
            .setHeader(retryHeader, retryCount)
            .build()

        rabbitTemplate.send(
            messagingProperties.deadLetterExchange,
            messagingProperties.deadLetterRoutingKey,
            dlqMessage
        )
    }
}
