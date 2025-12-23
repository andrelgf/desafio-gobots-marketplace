package com.gobots.marketplace_service.application.service.impl

import com.gobots.marketplace_service.application.exception.OrderNotFoundException
import com.gobots.marketplace_service.application.exception.StateTransitionNotAllowed
import com.gobots.marketplace_service.application.service.OutboxService
import com.gobots.marketplace_service.domain.model.Order
import com.gobots.marketplace_service.domain.model.OrderItem
import com.gobots.marketplace_service.domain.model.OrderStatus
import com.gobots.marketplace_service.domain.repository.OrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertEquals

class OrderServiceImplTest {
    private val orderRepository: OrderRepository = mockk()
    private val outboxService: OutboxService = mockk(relaxed = true)
    private val service = OrderServiceImpl(orderRepository, outboxService)

    @Test
    fun createOrder_whenItemsEmpty_throwsIllegalArgumentException() {
        assertThrows<IllegalArgumentException> {
            service.createOrder("store-1", emptyList())
        }
    }

    @Test
    fun createOrder_persistsOrderAndEnqueuesOutboxEvent() {
        val items = listOf(
            OrderItem("Widget", 2, BigDecimal("10.00")),
            OrderItem("Gadget", 1, BigDecimal("5.50"))
        )

        every { orderRepository.save(any()) } answers {
            val order = firstArg<Order>()
            order.id = 100L
            order
        }

        val result = service.createOrder("store-1", items)

        assertEquals(100L, result.id)
        assertEquals(OrderStatus.CREATED, result.status)
        assertEquals(BigDecimal("25.50"), result.totalAmount)
        verify { orderRepository.save(any()) }
        verify { outboxService.enqueue(100L, "store-1", OrderStatus.CREATED) }
    }

    @Test
    fun getById_whenMissing_throwsOrderNotFoundException() {
        every { orderRepository.findWithItemsById(1L) } returns Optional.empty()

        val exception = assertThrows<OrderNotFoundException> {
            service.getById(1L)
        }

        assertEquals(1L, exception.orderId)
    }

    @Test
    fun updateOrderStatusById_whenTransitionNotAllowed_throwsStateTransitionNotAllowed() {
        val order = Order("store-1").apply {
            id = 10L
            status = OrderStatus.CREATED
        }

        every { orderRepository.findWithItemsById(10L) } returns Optional.of(order)

        val exception = assertThrows<StateTransitionNotAllowed> {
            service.updateOrderStatusById(10L, OrderStatus.COMPLETED)
        }

        assertEquals(OrderStatus.CREATED, exception.current)
        assertEquals(OrderStatus.COMPLETED, exception.target)
    }

    @Test
    fun updateOrderStatusById_updatesOrderAndEnqueuesOutboxEvent() {
        val order = Order("store-1").apply {
            id = 10L
            status = OrderStatus.CREATED
        }

        every { orderRepository.findWithItemsById(10L) } returns Optional.of(order)
        every { orderRepository.save(any()) } answers {
            firstArg<Order>()
        }

        val result = service.updateOrderStatusById(10L, OrderStatus.PAID)

        assertEquals(OrderStatus.PAID, result.status)
        verify { orderRepository.save(any()) }
        verify { outboxService.enqueue(10L, "store-1", OrderStatus.PAID) }
    }
}
