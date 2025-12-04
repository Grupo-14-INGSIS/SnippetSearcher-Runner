package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.TestRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import com.grupo14IngSis.snippetSearcherRunner.service.TestingService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class TestingJobControllerTest {
    private val assetServiceClient: AssetServiceClient = mockk()
    private val testingService: TestingService = mockk()
    private val testingJobController = TestingJobController(assetServiceClient, testingService)

    @Test
    fun `testSnippet should return 200 with test results`() {
        val request = TestRequest("snippetId", listOf("input1"), "expected")
        val testResponse = TestResponse("actual", TestResult.PASSED)
        every { testingService.testSnippet(any(), any(), any()) } returns testResponse

        val response = testingJobController.testSnippet(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(testResponse, response.body)
    }

    @Test
    fun `testSnippet should return 500 on exception`() {
        val request = TestRequest("snippetId", listOf("input1"), "expected")
        val errorMessage = "Test exception"
        every { testingService.testSnippet(any(), any(), any()) } throws Exception(errorMessage)

        val response = testingJobController.testSnippet(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(errorMessage, response.body)
    }
}
