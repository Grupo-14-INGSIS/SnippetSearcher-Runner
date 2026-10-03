package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.CancelExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.InputRequest
import com.grupo14IngSis.snippetSearcherRunner.service.ExecutionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/snippets/{snippetId}/executions")
class ExecutionController(
    private val executionService: ExecutionService,
) {
    /**
     * POST    /api/v1/snippets/{snippetId}/executions
     *
     * Starts the execution of a snippet
     */
    @PostMapping
    fun startSnippetExecution(
        @PathVariable snippetId: String,
        @RequestBody request: ExecutionRequest,
    ): ResponseEntity<ExecutionResponse> {
        val execution =
            executionService.executeSnippet(
                snippetId,
                request.userId,
                request.version,
                request.environment,
            )
        return ResponseEntity.ok().body(execution)
    }

    /**
     * POST    /api/v1/snippets/{snippetId}/executions/input
     *
     * Give input to an execution
     */
    @PostMapping("/input")
    fun sendInput(
        @PathVariable snippetId: String,
        @RequestBody request: InputRequest,
    ): ResponseEntity<Void> {
        executionService.sendInput(snippetId, request.userId, request.input)
        return ResponseEntity.noContent().build()
    }

    /**
     * DELETE  /api/v1/snippets/{snippetId}/executions
     *
     * Cancel the execution of a snippet
     */
    @DeleteMapping
    fun cancelExecution(
        @PathVariable snippetId: String,
        @RequestBody request: CancelExecutionRequest,
    ): ResponseEntity<Void> {
        executionService.cancelExecution(snippetId, request.userId)
        return ResponseEntity.noContent().build()
    }

    /**
     * GET     /api/v1/snippets/{snippetId}/executions/status
     *
     * Get the current status of a snippet execution.
     */
    @GetMapping("/status")
    fun getExecutionStatus(
        @PathVariable snippetId: String,
    ): ResponseEntity<ExecutionResponse> {
        return ResponseEntity.ok().body(ExecutionResponse(ExecutionEventType.COMPLETED, listOf("Mock execution status")))
    }
}
