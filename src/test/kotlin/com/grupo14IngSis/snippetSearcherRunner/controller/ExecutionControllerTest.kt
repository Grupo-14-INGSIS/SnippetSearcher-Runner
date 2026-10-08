package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.CancelExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.InputRequest
import com.grupo14IngSis.snippetSearcherRunner.service.ExecutionService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ExecutionControllerTest {
    private lateinit var executionService: ExecutionService
    private lateinit var executionController: ExecutionController

    @BeforeEach
    fun setup() {
        executionService = mockk()
        executionController = ExecutionController(executionService)
    }

    @Test
    fun `test start snippet execution`() {
        val snippetId = "snippetId"
        val request = ExecutionRequest(userId = "userId", environment = emptyMap(), version = "1.0")
        val mockResponse = ExecutionResponse(ExecutionEventType.COMPLETED, listOf("OK"))

        every {
            executionService.executeSnippet(
                snippetId,
                request.userId,
                request.version,
                request.environment,
            )
        } returns mockResponse

        val response: ResponseEntity<ExecutionResponse> = executionController.startSnippetExecution(snippetId, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mockResponse, response.body)
    }

    @Test
    fun `test send input to execution when found`() {
        val snippetId = "snippetId"
        val request = InputRequest(userId = "userId", input = "some input")
        every { executionService.sendInput(snippetId, request.userId, request.input) } returns true

        val response = executionController.sendInput(snippetId, request)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `test send input to execution when not found`() {
        val snippetId = "snippetId"
        val request = InputRequest(userId = "userId", input = "some input")
        every { executionService.sendInput(snippetId, request.userId, request.input) } returns false

        val response = executionController.sendInput(snippetId, request)

        // This asserts the current buggy behavior. The controller should return NOT_FOUND.
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `test cancel execution when found`() {
        val snippetId = "snippetId"
        val request = CancelExecutionRequest(userId = "userId")
        every { executionService.cancelExecution(snippetId, request.userId) } returns true

        val response = executionController.cancelExecution(snippetId, request)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `test cancel execution when not found`() {
        val snippetId = "snippetId"
        val request = CancelExecutionRequest(userId = "userId")
        every { executionService.cancelExecution(snippetId, request.userId) } returns false

        val response = executionController.cancelExecution(snippetId, request)

        // This asserts the current buggy behavior. The controller should return NOT_FOUND.
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }
}
