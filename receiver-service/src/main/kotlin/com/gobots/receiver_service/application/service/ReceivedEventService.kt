package com.gobots.receiver_service.application.service

import com.gobots.receiver_service.domain.model.OrderEventPayload
import com.gobots.receiver_service.domain.model.ReceivedEvent

interface ReceivedEventService {
    fun handle(payload: OrderEventPayload)
    fun listAll(): List<ReceivedEvent>
}
