package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.TestRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetExecution
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/testing")
class TestingJobController(
    private val assetService: AssetServiceClient,
) {
    /**
     * POST   /api/v1/testing
     *
     * Start a test
     *
     * Request:
     *
     *     {
     *         snippetId: String,
     *         userId: String,
     *         version: String,
     *         environment: Map<String, String>,
     *         input: List<String>,
     *         expected: List<String>,
     *     }
     *
     * Response:
     *
     *     {
     *         actual: String,
     *         status: SUCCESS/FAILED/ERROR
     *         message: String
     *     }
     */
    @PostMapping("")
    fun testSnippet(
        @RequestBody request: TestRequest,
    ): ResponseEntity<TestResponse> {
        val result: TestResponse
        val execution =
            SnippetExecution(
                request.snippetId,
                request.version,
                request.environment,
                assetService,
            )

        execution.sendMultipleInputs(request.input)

        var completed = false
        execution.start()
        while (!completed) {
            completed = !execution.isRunning() // && execution.getStatus() != null
        }
        val status = execution.getStatus()
        if (status == ExecutionEventType.COMPLETED) {
            val actual = execution.getOutput().toMutableList()
            actual.removeLast()
            result =
                if (actual == request.expected) {
                    TestResponse(
                        actual,
                        TestResult.SUCCESS,
                        "Test succeeded",
                    )
                } else {
                    TestResponse(
                        actual,
                        TestResult.FAILED,
                        "Test failed: expected ${request.expected} but received $actual",
                    )
                }
            return ResponseEntity.ok().body(result)
        } else {
            result =
                TestResponse(
                    emptyList(),
                    TestResult.ERROR,
                    "Error while executing test",
                )
            return ResponseEntity.ok().body(result)
        }
    }
}
