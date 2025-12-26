package com.gobots.receiver_service.api.v1.controller

import com.gobots.receiver_service.api.v1.controller.docs.OrderSnapshotControllerDocs
import com.gobots.receiver_service.api.v1.dto.response.OrderSnapshotDTO
import com.gobots.receiver_service.api.v1.mapper.OrderSnapshotMapper
import com.gobots.receiver_service.application.service.OrderSnapshotService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/order-snapshots")
class OrderSnapshotController(
    private val orderSnapshotService: OrderSnapshotService,
    private val orderSnapshotMapper: OrderSnapshotMapper
) : OrderSnapshotControllerDocs {

    @GetMapping
    override fun listAll(): ResponseEntity<List<OrderSnapshotDTO>> {
        val snapshots = orderSnapshotService.listAll()
        val response = snapshots.map(orderSnapshotMapper::toDTO)
        return ResponseEntity.ok(response)
    }
}
