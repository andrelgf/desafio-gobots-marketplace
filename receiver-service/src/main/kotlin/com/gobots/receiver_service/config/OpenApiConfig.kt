package com.gobots.receiver_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI().apply {
            info = Info().apply {
                title = "Receiver Service"
                license = License().apply {
                    name = "Apache 2.0"
                }
            }
        }
}
