package com.gobots.receiver_service.domain.model

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
@Table(name = "received_events", schema = "receiver")
class ReceivedEvent(
    @field:Column(name = "event_id", nullable = false, unique = true)
    var eventId: UUID = UUID(0, 0),

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "event_type", nullable = false)
    var eventType: OrderEventType = OrderEventType.ORDER_CREATED,

    @field:Column(name = "order_id", nullable = false)
    var orderId: Long = 0,

    @field:Column(name = "store_code", nullable = false)
    var storeCode: String = "",

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    var payload: JsonNode? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
    }
}
