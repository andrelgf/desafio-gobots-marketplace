package com.gobots.receiver_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReceiverServiceApplication

fun main(args: Array<String>) {
	runApplication<ReceiverServiceApplication>(*args)
}
