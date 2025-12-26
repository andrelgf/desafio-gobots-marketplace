package com.gobots.receiver_service.api.v1.controller.docs

import com.gobots.receiver_service.api.v1.dto.response.OrderSnapshotDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@Tag(name = "Order Snapshots", description = "Operations for listing order snapshots")
interface OrderSnapshotControllerDocs {

    @Operation(summary = "List order snapshots", description = "Returns all captured order snapshots")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Order snapshots listed",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = OrderSnapshotDTO::class)
                )]
            )
        ]
    )
    fun listAll(): ResponseEntity<List<OrderSnapshotDTO>>
}
