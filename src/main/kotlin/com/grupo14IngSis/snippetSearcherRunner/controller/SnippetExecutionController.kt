package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetExecutionService
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asPublisher

@RestController
@RequestMapping("/api/execution")
class SnippetExecutionController(
    private val executionService: SnippetExecutionService
) {

    @PostMapping("/start")
    suspend fun startExecution(
        @RequestBody request: SnippetExecutionRequest
    ): Map<String, String> {
        val executionId = executionService.startExecution(request)
        return mapOf("executionId" to executionId)
    }

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

    @PostMapping("/input")
    suspend fun provideInput(
        @RequestBody input: SnippetExecutionInput
    ) {
        executionService.provideInput(input.executionId, input.input)
    }

    @DeleteMapping("/{executionId}")
    suspend fun cancelExecution(
        @PathVariable executionId: String
    ) {
        executionService.cancelExecution(executionId)
    }
}