package com.gobots.marketplace_service.application.exception

class OrderNotFoundException (val orderId: Long) : RuntimeException("Order $orderId not found")