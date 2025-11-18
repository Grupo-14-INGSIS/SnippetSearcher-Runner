package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetExecutionService
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asPublisher
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/v1/execution")
class SnippetRunnerController(
    private val executionService: SnippetExecutionService
) {
    /*
    POST   /api/v1/execution/run
    Start execution of a snippet
    body:
    {
        snippetId{snippetId},
        snippet:{snippet}
    }
    response:
    {
        executionId:{executionId},
        snippetId:{snippetId},
        status:RUNNING/WAITING/PENDING/CANCELLED/FINISHED/ERROR
    }
    */
    @PostMapping("/run")
    suspend fun startExecution(
        @RequestBody request: SnippetExecutionRequest
    ): ResponseEntity<SnippetExecutionStatusResponse> {
        val executionId = executionService.startExecution(request)
        return mapOf("executionId" to executionId)
    }

    /*
    GET    /api/v1/execution/{executionId}/status
    Get snippet execution status:
    response:
    {
        executionId:{executionId}
        snippetId: {snippetId},
        status:RUNNING/WAITING/PENDING/CANCELLED/FINISHED/ERROR
    }
     */
    @GetMapping(value = ["/events/{executionId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamEvents(
       @PathVariable executionId: String
    ): Flux<ServerSentEvent<SnippetExecutionEvent>> {
        val eventFlow = executionService.getEventFlow(executionId)
            ?: throw IllegalArgumentException("Execution not found")

        return Flux.from(eventFlow.map { event ->
            ServerSentEvent.builder(event)
                .id(System.currentTimeMillis().toString())
                .event(when (event) {
                    is SnippetExecutionEvent.Output -> "output"
                    is SnippetExecutionEvent.InputRequest -> "input_request"
                    is SnippetExecutionEvent.Error -> "error"
                    is SnippetExecutionEvent.Completed -> "completed"
                })
                .build()
        }.asPublisher())
    }
    /*
    POST   /api/v1/execution/{executionId}/input
    Give input for the execution
    body:
    {
        executionId:{executionId}
        snippetId:{snippetId},
        input:{input}
    }
     */
    @PostMapping("{executionId}/input")
    suspend fun provideInput(
        @RequestBody input: SnippetExecutionInput
    ) {
        executionService.provideInput(input.executionId, input.input)
    }

    /*
        DELETE /api/v1/execution/{executionId}
        Cancel execution of a snippet
     */
    @DeleteMapping("/{executionId}")
    suspend fun cancelExecution(
        @PathVariable executionId: String
    ): ResponseEntity<*> {
        executionService.cancelExecution(executionId)
    }
}