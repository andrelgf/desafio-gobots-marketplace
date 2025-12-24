package com.gobots.receiver_service.domain.model

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "order_snapshots", schema = "receiver")
class OrderSnapshot(
    @field:Column(name = "order_id", nullable = false)
    var orderId: Long = 0,

    @field:Column(name = "event_id", nullable = false, unique = true)
    var eventId: UUID = UUID(0, 0),

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "snapshot", nullable = false, columnDefinition = "jsonb")
    var snapshot: JsonNode? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "captured_at", nullable = false, updatable = false)
    var capturedAt: Instant? = null

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (capturedAt == null) capturedAt = now
    }
}
