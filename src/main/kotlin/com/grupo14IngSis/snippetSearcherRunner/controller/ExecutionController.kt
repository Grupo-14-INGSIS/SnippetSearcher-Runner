package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.service.ExecutionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/snippet")
class ExecutionController(
    private val executionService: ExecutionService,
) {
    /**
     * POST   /api/v1/snippets/{snippetId}/execution
     *
     * Start the execution of a snippet
     *
     * Response:
     *
     * {
     * status: FINISHED/WAITING/ERROR
     * }
     */
    @PostMapping("/snippets/{snippetId}/execution?version={version}")
    fun startSnippetExecution(
        @PathVariable snippetId: String,
        @PathVariable(required = false) version: String,
    ): ResponseEntity<ExecutionResponse> {
        val execution = executionService.executeSnippet(snippetId, version)

        return ResponseEntity.ok().body(execution)
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/execution/input
     *
     * Give input for an execution
     *
     * The request is a plain String
     *
     * Response:
     *
     * {
     * status: FINISHED/WAITING/ERROR
     * }
     */

    /**
     * DELETE /api/v1/snippets/{snippetId}/execution
     *
     * Cancel the execution of a snippet
     */
}
