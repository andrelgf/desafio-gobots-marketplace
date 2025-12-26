package com.gobots.receiver_service.api.v1.controller

import com.gobots.receiver_service.api.v1.controller.docs.SubscriptionControllerDocs
import com.gobots.receiver_service.api.v1.dto.request.SubscribeStoreRequest
import com.gobots.receiver_service.api.v1.dto.response.SubscriptionDTO
import com.gobots.receiver_service.api.v1.mapper.SubscriptionMapper
import com.gobots.receiver_service.application.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions")
class SubscriptionController(
    private val subscriptionMapper: SubscriptionMapper,
    private val subscriptionService: SubscriptionService
) : SubscriptionControllerDocs {

    @PostMapping
    override fun subscribe(@Valid @RequestBody request: SubscribeStoreRequest): ResponseEntity<List<SubscriptionDTO>> {
        val subscriptions = subscriptionService.subscribe(request.storeIds)
        val response = subscriptions.map { subscriptionMapper.toDTO(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping
    override fun listAll(): ResponseEntity<List<SubscriptionDTO>> {
        val subscriptions = subscriptionService.listAll()
        val response = subscriptions.map { subscriptionMapper.toDTO(it) }
        return ResponseEntity.ok(response)
    }
}
