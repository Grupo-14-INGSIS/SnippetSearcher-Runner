package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.CancelExecutionRequest
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/snippets/{snippetId}/executions")
class ExecutionController(
    private val executionService: ExecutionService,
) {
    /**
     * POST    /api/v1/snippets/{snippetId}/executions
     *
     * Starts the execution of a snippet with the inputs provided so far. Returns COMPLETED/ERROR with
     * the full output, or WAITING (with the output so far) when the snippet needs another `readInput`:
     * the client must call again with the extended `inputs` list.
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
                request.inputs,
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
     * GET     /api/v1/snippets/{snippetId}/executions/status?userId={userId}
     *
     * Get the current status of a snippet execution.
     */
    @GetMapping("/status")
    fun getExecutionStatus(
        @PathVariable snippetId: String,
        @RequestParam(required = false) userId: String?,
    ): ResponseEntity<ExecutionResponse> = ResponseEntity.ok().body(executionService.getExecutionStatus(snippetId, userId))
}
