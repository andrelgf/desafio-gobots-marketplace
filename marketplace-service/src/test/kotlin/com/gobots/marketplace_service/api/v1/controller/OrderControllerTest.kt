package com.gobots.marketplace_service.api.v1.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.marketplace_service.support.AbstractIntegrationTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderControllerTest : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    private fun toJson(value: Any): String = objectMapper.writeValueAsString(value)

    @Test
    fun createOrder_returnsCreatedAndBody() {
        val body = toJson(
            mapOf(
            "storeCode" to "store-1",
            "items" to listOf(
                mapOf(
                    "productName" to "Widget",
                    "quantity" to 2,
                    "unitPrice" to BigDecimal("10.00")
                ),
                mapOf(
                    "productName" to "Gadget",
                    "quantity" to 1,
                    "unitPrice" to BigDecimal("5.50")
                )
            )
            )
        )

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val location = response.header("Location")
        assertNotNull(location)

        val id = response.jsonPath().getLong("id")
        val status = response.jsonPath().getString("status")
        val totalAmount = response.jsonPath().getDouble("totalAmount")
        val itemsSize = response.jsonPath().getList<Any>("items").size

        assertEquals("CREATED", status)
        assertEquals(25.5, totalAmount, 0.001)
        assertEquals(2, itemsSize)

        RestAssured.given()
            .accept(ContentType.JSON)
            .`when`()
            .get("/api/v1/orders/$id")
            .then()
            .statusCode(200)
    }

    @Test
    fun getById_whenMissing_returnsNotFound() {
        val missingId = Long.MAX_VALUE

        val response = RestAssured.given()
            .accept(ContentType.JSON)
            .`when`()
            .get("/api/v1/orders/$missingId")
            .then()
            .statusCode(404)
            .extract()
            .response()

        val status = response.jsonPath().getInt("status")
        val path = response.jsonPath().getString("path")
        assertEquals(404, status)
        assertEquals("/api/v1/orders/$missingId", path)
    }

    @Test
    fun updateOrderStatus_updatesStatus() {
        val createBody = toJson(
            mapOf(
            "storeCode" to "store-1",
            "items" to listOf(
                mapOf(
                    "productName" to "Widget",
                    "quantity" to 1,
                    "unitPrice" to BigDecimal("10.00")
                )
            )
            )
        )

        val id = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createBody)
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id")

        val updateBody = toJson(mapOf("status" to "PAID"))

        val status = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateBody)
            .`when`()
            .patch("/api/v1/orders/$id")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("status")

        assertEquals("PAID", status)
    }

    @Test
    fun updateOrderStatus_whenTransitionInvalid_returnsConflict() {
        val createBody = toJson(
            mapOf(
            "storeCode" to "store-1",
            "items" to listOf(
                mapOf(
                    "productName" to "Widget",
                    "quantity" to 1,
                    "unitPrice" to BigDecimal("10.00")
                )
            )
            )
        )

        val id = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createBody)
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id")

        val updateBody = toJson(mapOf("status" to "COMPLETED"))

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateBody)
            .`when`()
            .patch("/api/v1/orders/$id")
            .then()
            .statusCode(409)
            .extract()
            .response()

        val status = response.jsonPath().getInt("status")
        val path = response.jsonPath().getString("path")
        assertEquals(409, status)
        assertEquals("/api/v1/orders/$id", path)
    }

    @Test
    fun listAll_returnsOrders() {
        val body = toJson(
            mapOf(
                "storeCode" to "store-1",
                "items" to listOf(
                    mapOf(
                        "productName" to "Widget",
                        "quantity" to 1,
                        "unitPrice" to BigDecimal("10.00")
                    )
                )
            )
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)

        val response = RestAssured.given()
            .accept(ContentType.JSON)
            .`when`()
            .get("/api/v1/orders")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val size = response.jsonPath().getList<Any>("").size
        assertEquals(1, size)
    }
}
