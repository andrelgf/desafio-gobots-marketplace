package com.gobots.receiver_service.api.v1.controller.docs

import com.gobots.receiver_service.api.v1.dto.response.ReceivedEventDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@Tag(name = "Received Events", description = "Operations for listing received events")
interface ReceivedEventControllerDocs {

    @Operation(summary = "List received events", description = "Returns all received events")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Received events listed",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ReceivedEventDTO::class)
                )]
            )
        ]
    )
    fun listAll(): ResponseEntity<List<ReceivedEventDTO>>
}
