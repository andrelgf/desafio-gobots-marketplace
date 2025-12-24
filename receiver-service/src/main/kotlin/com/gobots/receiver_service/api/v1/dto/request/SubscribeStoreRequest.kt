package com.gobots.receiver_service.api.v1.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class SubscribeStoreRequest(
    @field:NotEmpty(message = "storeIds is required")
    val storeIds: List<@NotBlank(message = "storeIds must not contain blank values") String>
)
