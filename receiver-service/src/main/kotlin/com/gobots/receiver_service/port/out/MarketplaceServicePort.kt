package com.gobots.receiver_service.port.out

import com.fasterxml.jackson.databind.JsonNode

interface MarketplaceServicePort{
    fun getOrder(id: Long): JsonNode
}