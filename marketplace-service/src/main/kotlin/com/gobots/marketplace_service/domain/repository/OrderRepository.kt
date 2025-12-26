package com.gobots.marketplace_service.domain.repository

import com.gobots.marketplace_service.domain.model.Order
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @Query("select distinct o from Order o left join fetch o.itemsInternal")
    fun findAllWithItems(): List<Order>

    @EntityGraph(attributePaths = ["itemsInternal"])
    fun findWithItemsById(id: Long): Optional<Order>
}
