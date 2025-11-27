package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.TestingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/testing")
class TestingJobController(
    private val assetServiceClient: AssetServiceClient
) {
    private val testingService = TestingService()

    /**
     * POST   /api/v1/testing
     *
     * Start a test
     *
     * Request:
     *
     *     {
     *         snippetId: String,
     *         input: [String],
     *         expected: String,
     *     }
     *
     * Response:
     *
     *     {
     *         actual: String,
     *         Status: PASSED/FAILED/ERROR
     *     }
     */
    @PostMapping("")
    fun testSnippet(
        @RequestBody request: TestRequest
    ): ResponseEntity<Any> {
        return try {
            val result = testingService.testSnippet(request.snippetId, request.input, request.expected)
            ResponseEntity.ok().body(result)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(e.message)
        }
    }
}