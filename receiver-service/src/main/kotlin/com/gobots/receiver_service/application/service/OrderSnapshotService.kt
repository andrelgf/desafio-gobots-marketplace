package com.gobots.receiver_service.application.service

import com.gobots.receiver_service.domain.model.OrderSnapshot

interface OrderSnapshotService {
    fun listAll(): List<OrderSnapshot>
}
