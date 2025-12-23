package com.gobots.marketplace_service.domain.repository

import com.gobots.marketplace_service.domain.model.Order
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = ["itemsInternal"])
    fun findWithItemsById(id: Long): Optional<Order>
}
