package com.gobots.receiver_service.domain.repository

import com.gobots.receiver_service.domain.model.ReceivedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReceivedEventRepository : JpaRepository<ReceivedEvent, Long> {
    fun existsByEventId(eventId: UUID): Boolean
}
