package com.grupo14IngSis.snippetSearcherRunner.advice

import com.grupo14IngSis.snippetSearcherRunner.dto.ErrorResponse
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        // Log the warning, as Bad Requests are often client errors, not system errors.
        logger.warn("Bad request: ${ex.message}")

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Invalid request",
                details = ex.message ?: "No details available",
            )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        // Log the error with the full stack trace, as it's an unexpected system error.
        logger.error("Unhandled exception occurred", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected internal server error occurred.",
                details = ex.message ?: "No details available",
            )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
