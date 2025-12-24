package com.gobots.receiver_service.infrastructure.marketplace.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.gobots.receiver_service.application.exception.MarketplaceOrderNotFoundException
import com.gobots.receiver_service.application.exception.MarketplaceServiceUnavailableException
import com.gobots.receiver_service.infrastructure.marketplace.client.MarketplaceClient
import com.gobots.receiver_service.port.out.MarketplaceServicePort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MarketPlaceFeignAdapter(
    private val marketplaceClient: MarketplaceClient
) : MarketplaceServicePort {
    private val logger = LoggerFactory.getLogger(MarketPlaceFeignAdapter::class.java)

    @CircuitBreaker(name = "marketplaceService", fallbackMethod = "fallback")
    @Retry(name = "marketplaceService")
    override fun getOrder(id: Long): JsonNode {
        try {
            logger.debug("Requesting marketplace order id={}", id)
            return marketplaceClient.getOrder(id)
        } catch (ex: FeignException.NotFound) {
            logger.warn("Marketplace order not found id={}", id)
            throw MarketplaceOrderNotFoundException(id)
        } catch (ex: MarketplaceOrderNotFoundException) {
            throw ex
        } catch (ex: Exception) {
            logger.warn("marketplace-service unavailable. msg={}", ex.message)
            throw MarketplaceServiceUnavailableException("marketplace-service unavailable", ex)
        }
    }

    @Suppress("unused")
    private fun fallback(id: Long, ex: Throwable): JsonNode {
        logger.warn("Marketplace fallback for order id={} cause={}", id, ex::class.simpleName)
        if (ex is MarketplaceOrderNotFoundException) {
            throw ex
        }
        throw MarketplaceServiceUnavailableException("marketplace-service unavailable (fallback)", ex)
    }
}
