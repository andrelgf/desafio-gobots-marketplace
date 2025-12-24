package com.gobots.receiver_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients(basePackages = ["com.gobots.receiver_service.infrastructure.marketplace.client"])
class ReceiverServiceApplication

fun main(args: Array<String>) {
	runApplication<ReceiverServiceApplication>(*args)
}
