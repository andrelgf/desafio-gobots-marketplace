package com.gobots.marketplace_service.api.v1.controller.docs

import com.gobots.marketplace_service.api.v1.dto.request.CreateOrderRequest
import com.gobots.marketplace_service.api.v1.dto.request.UpdateOrderStatusRequest
import com.gobots.marketplace_service.api.v1.dto.response.OrderDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Orders", description = "Operations for creating and managing orders")
interface OrderControllerDocs {

    @Operation(summary = "Create an order", description = "Creates a new order and returns the created resource")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Order created",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = OrderDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data")
        ]
    )
    fun createOrder(@RequestBody createOrderRequest: CreateOrderRequest): ResponseEntity<OrderDTO>

    @Operation(summary = "Get order by id", description = "Returns a single order by its id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Order found",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = OrderDTO::class)
                )]
            ),
            ApiResponse(responseCode = "404", description = "Order not found")
        ]
    )
    fun getById(@PathVariable id: Long): ResponseEntity<OrderDTO>

    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Order updated",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = OrderDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "404", description = "Order not found"),
            ApiResponse(responseCode = "409", description = "Invalid status transition")
        ]
    )
    fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestBody newStatusRequest: UpdateOrderStatusRequest
    ): ResponseEntity<OrderDTO>
}