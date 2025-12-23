package com.gobots.marketplace_service.domain.repository

import com.gobots.marketplace_service.domain.model.OutboxEvents
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OutboxEventsRepository: JpaRepository<OutboxEvents, Long> {
}