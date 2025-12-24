package com.gobots.receiver_service.infrastructure.marketplace.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "marketplaceClient",
    url = "\${app.marketplace.base-url}"
)
interface MarketplaceClient {
    @GetMapping("/api/v1/orders/{id}")
    fun getOrder(@PathVariable("id") id: Long): JsonNode
}