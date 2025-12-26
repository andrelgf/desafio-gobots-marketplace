package com.gobots.marketplace_service.application.service.impl

import com.gobots.marketplace_service.application.exception.OrderNotFoundException
import com.gobots.marketplace_service.application.exception.StateTransitionNotAllowed
import com.gobots.marketplace_service.application.service.OrderService
import com.gobots.marketplace_service.application.service.OutboxService
import com.gobots.marketplace_service.domain.model.Order
import com.gobots.marketplace_service.domain.model.OrderItem
import com.gobots.marketplace_service.domain.model.OrderStatus
import com.gobots.marketplace_service.domain.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderServiceImpl(
    private  val  orderRepository: OrderRepository,
    private  val outboxService: OutboxService
    ): OrderService {
    companion object {
        private val log = LoggerFactory.getLogger(OrderServiceImpl::class.java)
    }

    @Transactional
    override fun createOrder(storeCode: String, items: List<OrderItem>): Order {
        if (items.isEmpty()) {
            throw IllegalArgumentException("Order must have at least one item")
        }

        val order = Order(storeCode = storeCode)

        items.forEach { i ->
            order.addItem(i.productName, i.unitPrice, i.quantity )
        }

        order.totalAmount = items.fold(BigDecimal.ZERO) { acc, it -> acc + it.totalAmount }

        val saved = orderRepository.save(order)

        outboxService.enqueue(
            orderId = saved.id ?: error("Order id should not be null after save"),
            storeCode = saved.storeCode,
            newStatus = saved.status)

        return saved
    }

    @Transactional
    override fun listAll(): List<Order> {
        return orderRepository.findAllWithItems()
    }
    @Transactional
    override fun getById(id: Long): Order {
        return orderRepository.findWithItemsById(id).orElseThrow { OrderNotFoundException(id) }
    }

    @Transactional
    override fun updateOrderStatusById(id: Long, newStatus: OrderStatus): Order {
        val order = orderRepository.findWithItemsById(id).orElseThrow { OrderNotFoundException(id) }

        if (!order.status.canTransitionTo(newStatus)) {
            throw StateTransitionNotAllowed(
                orderId = id,
                current = order.status,
                target = newStatus
            )
        }

        order.status = newStatus

        val saved = orderRepository.save(order)

        outboxService.enqueue(
            orderId = saved.id ?: error("Order id should not be null after save"),
            storeCode = saved.storeCode,
            newStatus = saved.status)


        return saved
    }
}
