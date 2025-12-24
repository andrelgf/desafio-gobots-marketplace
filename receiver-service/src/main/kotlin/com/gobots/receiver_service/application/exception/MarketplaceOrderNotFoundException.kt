package com.gobots.receiver_service.application.exception

class MarketplaceOrderNotFoundException(orderId: Long) :
    RuntimeException("Order $orderId not found in marketplace")
