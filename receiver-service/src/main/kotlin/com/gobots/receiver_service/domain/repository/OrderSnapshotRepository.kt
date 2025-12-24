package com.gobots.receiver_service.domain.repository

import com.gobots.receiver_service.domain.model.OrderSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderSnapshotRepository : JpaRepository<OrderSnapshot, Long>
