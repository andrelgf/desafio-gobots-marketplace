package com.gobots.receiver_service.application.service.impl

import com.gobots.receiver_service.application.service.OrderSnapshotService
import com.gobots.receiver_service.domain.model.OrderSnapshot
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderSnapshotServiceImpl(
    private val orderSnapshotRepository: OrderSnapshotRepository
) : OrderSnapshotService {

    @Transactional(readOnly = true)
    override fun listAll(): List<OrderSnapshot> {
        return orderSnapshotRepository.findAll()
    }
}
