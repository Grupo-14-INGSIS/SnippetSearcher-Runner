package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetStatusUpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AppClient(
    private val restTemplate: RestTemplate,
    @Value("\${app.app.url}") private val app: String,
) {
    private val logger = LoggerFactory.getLogger(AssetServiceClient::class.java)

    private fun defaultHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        return headers
    }

    fun updateSnippetTaskStatus(
        snippetId: String,
        userId: String,
        task: String,
        status: Boolean,
    ) {
        restTemplate.exchange(
            "$app/api/v1/snippets/$snippetId/status",
            HttpMethod.PATCH,
            HttpEntity(SnippetStatusUpdateRequest(userId, task, status), defaultHeaders()),
            String::class.java,
        )
    }

    fun registerSnippet(
        snippetId: String,
        userId: String,
        language: String,
    ): ResponseEntity<SnippetCreationResponse> {
        val response =
            restTemplate.exchange(
                "$app/api/v1/snippets/$snippetId?userId=$userId&language=$language",
                HttpMethod.PUT,
                HttpEntity<Void>(defaultHeaders()),
                SnippetCreationResponse::class.java,
            )
        return response
    }

    fun sendLine(
        snippetId: String,
        executionId: String,
        line: String,
        status: ExecutionEventType,
    ) {
        /*
        val message = ExecutionEvent(status, line)
        restTemplate.exchange(
            "$app/v1/snippets/$snippetId/run",
            HttpMethod.POST,
            HttpEntity(message, defaultHeaders()),
            Void::class.java,
        )
         */
    }
}
