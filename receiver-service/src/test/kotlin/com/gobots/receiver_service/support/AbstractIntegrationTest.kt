package com.gobots.receiver_service.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    companion object {

        @Container
        @JvmField
        val POSTGRES: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:16").apply {
                withDatabaseName("gobots")
                withUsername("gobots")
                withPassword("gobots")
            }

        @DynamicPropertySource
        @JvmStatic
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl)
            registry.add("spring.datasource.username", POSTGRES::getUsername)
            registry.add("spring.datasource.password", POSTGRES::getPassword)

            registry.add("spring.flyway.schemas") { "receiver" }
            registry.add("spring.flyway.default-schema") { "receiver" }
            registry.add("spring.jpa.properties.hibernate.default_schema") { "receiver" }
        }
    }
}
