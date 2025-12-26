package com.gobots.receiver_service.api.v1.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.gobots.receiver_service.domain.model.OrderEventType
import com.gobots.receiver_service.domain.model.OrderSnapshot
import com.gobots.receiver_service.domain.model.ReceivedEvent
import com.gobots.receiver_service.domain.repository.OrderSnapshotRepository
import com.gobots.receiver_service.domain.repository.ReceivedEventRepository
import com.gobots.receiver_service.support.AbstractIntegrationTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.util.UUID
import kotlin.test.assertEquals

class OrderSnapshotControllerTest : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderSnapshotRepository: OrderSnapshotRepository

    @Autowired
    private lateinit var receivedEventRepository: ReceivedEventRepository

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        orderSnapshotRepository.deleteAll()
        receivedEventRepository.deleteAll()
    }

    @Test
    fun listAll_returnsOrderSnapshots() {
        val eventId = UUID.randomUUID()
        val snapshotNode = objectMapper.createObjectNode()
            .put("id", 42L)
            .put("status", "CREATED")

        receivedEventRepository.save(
            ReceivedEvent(
                eventId = eventId,
                eventType = OrderEventType.ORDER_CREATED,
                orderId = 42L,
                storeCode = "STORE_042",
                payload = objectMapper.createObjectNode()
            )
        )

        val saved = orderSnapshotRepository.save(
            OrderSnapshot(
                orderId = 42L,
                eventId = eventId,
                snapshot = snapshotNode
            )
        )

        val response = RestAssured.given()
            .accept(ContentType.JSON)
            .`when`()
            .get("/api/v1/order-snapshots")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val size = response.jsonPath().getList<Any>("").size
        assertEquals(1, size)
        val returnedId = response.jsonPath().getLong("[0].id")
        assertEquals(saved.id, returnedId)
    }
}
