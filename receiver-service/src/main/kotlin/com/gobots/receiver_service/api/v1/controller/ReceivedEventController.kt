package com.gobots.receiver_service.api.v1.controller

import com.gobots.receiver_service.api.v1.controller.docs.ReceivedEventControllerDocs
import com.gobots.receiver_service.api.v1.dto.response.ReceivedEventDTO
import com.gobots.receiver_service.api.v1.mapper.ReceivedEventMapper
import com.gobots.receiver_service.application.service.ReceivedEventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/received-events")
class ReceivedEventController(
    private val receivedEventService: ReceivedEventService,
    private val receivedEventMapper: ReceivedEventMapper
) : ReceivedEventControllerDocs {

    @GetMapping
    override fun listAll(): ResponseEntity<List<ReceivedEventDTO>> {
        val events = receivedEventService.listAll()
        val response = events.map(receivedEventMapper::toDTO)
        return ResponseEntity.ok(response)
    }
}
