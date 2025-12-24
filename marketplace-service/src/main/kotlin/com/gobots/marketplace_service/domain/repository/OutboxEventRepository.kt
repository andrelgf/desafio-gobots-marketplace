package com.gobots.marketplace_service.domain.repository

import com.gobots.marketplace_service.domain.model.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.data.repository.query.Param

@Repository
interface OutboxEventRepository: JpaRepository<OutboxEvent, Long> {
    @Query(
        value = """
            SELECT *
            FROM marketplace.outbox_events
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun findPendingForUpdate(@Param("batchSize") batchSize: Int): List<OutboxEvent>
}
