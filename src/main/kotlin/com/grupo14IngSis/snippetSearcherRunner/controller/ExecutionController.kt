package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.CancelExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.InputRequest
import com.grupo14IngSis.snippetSearcherRunner.service.ExecutionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/snippet")
class ExecutionController(
    private val executionService: ExecutionService,
) {
    /**
     * POST   /api/v1/snippets/{snippetId}/run
     *
     * Start the execution of a snippet
     *
     * Request:
     *
     *     {
     *       userId: String,
     *       version: String,
     *       environment: Map<String, String>
     *     }
     *
     * Response:
     *
     *     {
     *        status: FINISHED/WAITING/ERROR
     *        message: String
     *     }
     */
    @PostMapping("/snippets/{snippetId}/run")
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
     * POST    /api/v1/snippets/{snippetId}/run/input
     *
     * Give input to an execution
     *
     * Request:
     *
     *     {
     *       val userId: String,
     *       val input: String?
     *     }
     */
    @PostMapping("/snippets/{snippetId}/run/input")
    fun sendInput(
        @PathVariable snippetId: String,
        @RequestBody request: InputRequest,
    ): ResponseEntity<Void> {
        executionService.sendInput(snippetId, request.userId, request.input)
        return ResponseEntity.noContent().build()
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}/run/input
     *
     * Cancel the execution of a snippet
     *
     * Request:
     *
     *     {
     *       userID: String
     *     }
     */
    @DeleteMapping("/snippets/{snippetId}/run")
    fun cancelExecution(
        @PathVariable snippetId: String,
        @RequestBody request: CancelExecutionRequest,
    ): ResponseEntity<Void> {
        executionService.cancelExecution(snippetId, request.userId)
        return ResponseEntity.noContent().build()
    }
}
