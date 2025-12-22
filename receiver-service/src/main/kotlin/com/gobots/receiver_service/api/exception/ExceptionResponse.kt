package com.gobots.receiver_service.api.exception

import java.time.Instant

data class ExceptionResponse(
    val status: Int,
    val message: String,
    val path: String,
    val timestamp: Instant
)
