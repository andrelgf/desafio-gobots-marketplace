package com.gobots.receiver_service.api.v1.controller.docs

import com.gobots.receiver_service.api.v1.dto.request.SubscribeStoreRequest
import com.gobots.receiver_service.api.v1.dto.response.SubscriptionDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Subscriptions", description = "Operations for subscribing stores")
interface SubscriptionControllerDocs {

    @Operation(summary = "Subscribe stores", description = "Registers store codes for subscription")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Subscriptions created",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = SubscriptionDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data")
        ]
    )
    fun subscribe(@RequestBody request: SubscribeStoreRequest): ResponseEntity<List<SubscriptionDTO>>

    @Operation(summary = "List subscriptions", description = "Returns all subscribed stores")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Subscriptions listed",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = SubscriptionDTO::class)
                )]
            )
        ]
    )
    fun listAll(): ResponseEntity<List<SubscriptionDTO>>
}
