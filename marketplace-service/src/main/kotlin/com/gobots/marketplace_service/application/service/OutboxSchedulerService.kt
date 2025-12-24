package com.gobots.marketplace_service.application.service

interface OutboxSchedulerService {
    fun pollAndPublish()
}
