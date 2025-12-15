package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.Snippet
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetRegistrationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetStatusUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
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

    fun getSnippet(snippetId: String): Snippet? {
        return restTemplate.exchange(
            "$app/api/v1/snippets/$snippetId",
            HttpMethod.GET,
            HttpEntity<Void>(defaultHeaders()),
            Snippet::class.java,
        ).body
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
        name: String,
        language: String,
    ): ResponseEntity<SnippetCreationResponse> {
        val request =
            SnippetRegistrationRequest(
                userId,
                name,
                language,
            )
        val response =
            restTemplate.exchange(
                "$app/api/v1/snippets/$snippetId",
                HttpMethod.PUT,
                HttpEntity<SnippetRegistrationRequest>(request, defaultHeaders()),
                SnippetCreationResponse::class.java,
            )
        return response
    }

    fun testAll(
        snippetId: String,
        jwt: String,
    ): List<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwt)

        val testIds =
            restTemplate.exchange(
                "$app/api/v1/snippets/$snippetId/tests",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                List::class.java,
            ).body ?: return emptyList()
        val results: MutableList<String> = mutableListOf()
        for (test in testIds) {
            val result =
                restTemplate.exchange(
                    "$app/api/v1/snippets/$snippetId/tests/$test",
                    HttpMethod.PUT,
                    HttpEntity<Void>(headers),
                    TestResponse::class.java,
                ).body ?: continue
            if (result.result != TestResult.SUCCESS) {
                results.add(
                    result.message,
                )
            }
        }
        return results
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
