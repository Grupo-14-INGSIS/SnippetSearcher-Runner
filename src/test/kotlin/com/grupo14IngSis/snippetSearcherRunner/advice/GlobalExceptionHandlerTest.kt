package com.grupo14IngSis.snippetSearcherRunner.advice

import com.grupo14IngSis.snippetSearcherRunner.dto.ErrorResponse
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandlerTest {
    private val globalExceptionHandler = GlobalExceptionHandler()
    private val webRequest = mockk<WebRequest>(relaxed = true)
    private val httpRequest = mockk<HttpServletRequest>()

    @Test
    fun `test handle bad request exception`() {
        every { httpRequest.method } returns "POST"
        every { httpRequest.requestURI } returns "/test"
        val exception = BadRequestException("Invalid request")
        val responseEntity = globalExceptionHandler.handleBadRequestException(exception, webRequest, httpRequest)

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)

        val errorResponse = responseEntity.body as ErrorResponse
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status)
        assertEquals("Invalid request", errorResponse.message)
    }

    @Test
    fun `test handle all exceptions`() {
        every { httpRequest.method } returns "POST"
        every { httpRequest.requestURI } returns "/test"
        val exception = Exception("Internal server error")
        val responseEntity = globalExceptionHandler.handleAllExceptions(exception, webRequest, httpRequest)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)

        val errorResponse = responseEntity.body as ErrorResponse
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.status)
        assertEquals("An unexpected internal server error occurred.", errorResponse.message)
        assertEquals("Internal server error", errorResponse.details)
    }
}
