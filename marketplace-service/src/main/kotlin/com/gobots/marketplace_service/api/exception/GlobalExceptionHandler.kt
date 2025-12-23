package com.gobots.marketplace_service.api.exception

import com.gobots.marketplace_service.application.exception.OrderNotFoundException
import com.gobots.marketplace_service.application.exception.StateTransitionNotAllowed
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = ex.bindingResult
            .fieldErrors
            .joinToString("; ") { err: FieldError ->
                "${err.field}: ${err.defaultMessage}"
            }
            .ifBlank { "Invalid data" }

        val body = buildExceptionResponse(HttpStatus.BAD_REQUEST, message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = "Invalid request: malformed JSON or incompatible fields"
        val body = buildExceptionResponse(HttpStatus.BAD_REQUEST, message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFound(
        ex: OrderNotFoundException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = ex.message ?: "Order not found"
        val body = buildExceptionResponse(HttpStatus.NOT_FOUND, message, request)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(StateTransitionNotAllowed::class)
    fun handleStateTransitionNotAllowed(
        ex: StateTransitionNotAllowed,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = ex.message ?: "Order status transition not allowed"
        val body = buildExceptionResponse(HttpStatus.CONFLICT, message, request)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = ex.message ?: "Invalid request"
        val body = buildExceptionResponse(HttpStatus.BAD_REQUEST, message, request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    private fun getPath(request: WebRequest): String =
        request.getDescription(false).removePrefix("uri=")

    private fun buildExceptionResponse(
        httpStatus: HttpStatus,
        message: String,
        request: WebRequest
    ): ExceptionResponse =
        ExceptionResponse(
            status = httpStatus.value(),
            message = message,
            path = getPath(request),
            timestamp = Instant.now()
        )
}
