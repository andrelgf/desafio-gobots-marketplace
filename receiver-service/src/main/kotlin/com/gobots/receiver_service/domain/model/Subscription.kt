package com.gobots.receiver_service.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "subscriptions", schema = "receiver")
class Subscription(
    @field:Column(name = "store_code", nullable = false, unique = true)
    var storeCode: String = ""
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
