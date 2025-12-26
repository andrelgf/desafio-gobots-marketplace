package com.gobots.receiver_service.api.v1.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import com.gobots.receiver_service.support.AbstractIntegrationTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals

class SubscriptionControllerTest : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        subscriptionRepository.deleteAll()
    }

    private fun toJson(value: Any): String = objectMapper.writeValueAsString(value)

    @Test
    fun subscribe_returnsCreatedSubscriptions() {
        val body = toJson(mapOf("storeIds" to listOf("store_001", "store_002")))

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/subscriptions")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val codes = response.jsonPath().getList<String>("storeCode").sorted()
        assertEquals(listOf("STORE_001", "STORE_002"), codes)
    }

    @Test
    fun subscribe_whenAlreadySubscribed_returnsEmptyList() {
        val body = toJson(mapOf("storeIds" to listOf("store_001")))

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/subscriptions")
            .then()
            .statusCode(200)

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/subscriptions")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val size = response.jsonPath().getList<Any>("").size
        assertEquals(0, size)
    }

    @Test
    fun subscribe_whenInvalid_returnsBadRequest() {
        val body = toJson(mapOf("storeIds" to listOf(" ")))

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/subscriptions")
            .then()
            .statusCode(400)
            .extract()
            .response()

        val status = response.jsonPath().getInt("status")
        val path = response.jsonPath().getString("path")
        assertEquals(400, status)
        assertEquals("/api/v1/subscriptions", path)
    }

    @Test
    fun listAll_returnsSubscriptions() {
        val body = toJson(mapOf("storeIds" to listOf("store_001", "store_002")))

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/subscriptions")
            .then()
            .statusCode(200)

        val response = RestAssured.given()
            .accept(ContentType.JSON)
            .`when`()
            .get("/api/v1/subscriptions")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val codes = response.jsonPath().getList<String>("storeCode").sorted()
        assertEquals(listOf("STORE_001", "STORE_002"), codes)
    }
}
