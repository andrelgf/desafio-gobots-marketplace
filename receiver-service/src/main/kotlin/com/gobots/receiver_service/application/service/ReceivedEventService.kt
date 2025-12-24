package com.gobots.receiver_service.application.service

import com.gobots.receiver_service.domain.model.OrderEventPayload

interface ReceivedEventService {
    fun handle(payload: OrderEventPayload)
}
