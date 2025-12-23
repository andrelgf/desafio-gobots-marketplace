package com.gobots.marketplace_service.api.v1.mapper

import com.gobots.marketplace_service.api.v1.dto.request.CreateOrderOrderItemDTO
import com.gobots.marketplace_service.api.v1.dto.response.OrderDTO
import com.gobots.marketplace_service.api.v1.dto.response.OrderItemDTO
import com.gobots.marketplace_service.domain.model.Order
import com.gobots.marketplace_service.domain.model.OrderItem
import org.springframework.stereotype.Component

@Component
class OrderMapper {
    fun toDTO(order: Order): OrderDTO {
        val id = requireNotNull(order.id) { "Order id cannot be null" }
        val createdAt = requireNotNull(order.createdAt) { "Order createdAt cannot be null" }

        return OrderDTO(
            id = id,
            status = order.status,
            storeCode = order.storeCode,
            items = order.items.map(::toOrderItemDTO),
            totalAmount = order.totalAmount,
            createdAt = createdAt,
            updatedAt = order.updatedAt
        )
    }

    fun toOrderItemDTO(orderItem: OrderItem): OrderItemDTO {
        val id = requireNotNull(orderItem.id) { "OrderItem id cannot be null" }

        return OrderItemDTO(
            id = id,
            productName = orderItem.productName,
            quantity = orderItem.quantity,
            unitPrice = orderItem.unitPrice,
            totalAmount = orderItem.totalAmount
        )
    }

    fun toOrderItemEntity(createOrderOrderItemDTO: CreateOrderOrderItemDTO): OrderItem {
        return OrderItem(
            productName = createOrderOrderItemDTO.productName,
            quantity = createOrderOrderItemDTO.quantity,
            unitPrice = createOrderOrderItemDTO.unitPrice
        )
    }

}