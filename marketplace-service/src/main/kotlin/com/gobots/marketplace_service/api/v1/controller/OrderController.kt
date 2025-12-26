package com.gobots.marketplace_service.api.v1.controller

import com.gobots.marketplace_service.api.v1.controller.docs.OrderControllerDocs
import com.gobots.marketplace_service.api.v1.dto.request.CreateOrderRequest
import com.gobots.marketplace_service.api.v1.dto.request.UpdateOrderStatusRequest
import com.gobots.marketplace_service.api.v1.dto.response.OrderDTO
import com.gobots.marketplace_service.api.v1.mapper.OrderMapper
import com.gobots.marketplace_service.application.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI


@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderMapper: OrderMapper,
    private val orderService: OrderService
) : OrderControllerDocs {

    @PostMapping
    override fun createOrder(@Valid @RequestBody createOrderRequest: CreateOrderRequest): ResponseEntity<OrderDTO> {

        val storeCode = createOrderRequest.storeCode
        val items = createOrderRequest.items.map { orderItemDTO -> orderMapper.toOrderItemEntity(orderItemDTO) }
        val order = orderService.createOrder(storeCode, items)
        val response = orderMapper.toDTO(order)

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id)
            .toUri()

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    override fun listAll(): ResponseEntity<List<OrderDTO>> {
        val orders = orderService.listAll()
        val response = orders.map(orderMapper::toDTO)
        return ResponseEntity.ok(response)
    }

    @GetMapping(value = ["/{id}"])
    override fun getById(@PathVariable id: Long): ResponseEntity<OrderDTO> {
        val order = orderService.getById(id)
        val response = orderMapper.toDTO(order)
        return ResponseEntity.ok<OrderDTO>(response)
    }

    @PatchMapping(value = ["/{id}"])
    override fun updateOrderStatus(@PathVariable id: Long, @Valid @RequestBody newStatusRequest: UpdateOrderStatusRequest): ResponseEntity<OrderDTO>{
        val order = orderService.updateOrderStatusById(id, newStatusRequest.status)
        val  response = orderMapper.toDTO(order)
        return ResponseEntity.ok<OrderDTO>(response)
    }


}
