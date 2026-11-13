package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

class AppClientTest {
    private lateinit var restTemplate: RestTemplate
    private lateinit var appClient: AppClient
    private val appUrl = "http://localhost:8080"

    @BeforeEach
    fun setUp() {
        restTemplate = mockk(relaxed = true)
        appClient = AppClient(restTemplate, appUrl)
    }

    @Test
    fun `requestLint calls the App lint endpoint forwarding the user JWT`() {
        appClient.requestLint("snippet123", "the-jwt")

        verify(exactly = 1) {
            restTemplate.exchange(
                "$appUrl/api/v1/snippets/snippet123/lint-jobs",
                HttpMethod.POST,
                match<HttpEntity<Void>> { it.headers.getFirst("Authorization") == "Bearer the-jwt" },
                Void::class.java,
            )
        }
    }

    @Test
    fun `requestLint propagates RestClientException`() {
        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Void>>(), Void::class.java)
        } throws RestClientException("Service unavailable")

        assertThrows(RestClientException::class.java) {
            appClient.requestLint("snippet1", "jwt")
        }
    }

    @Test
    fun `registerSnippet successfully registers the snippet`() {
        val snippetId = "snippet"
        val userId = "user"
        val name = "sniooet"
        val language = "language"
        val app = appUrl
        every {
            restTemplate.exchange(
                "$app/internal/snippets/$snippetId",
                HttpMethod.PUT,
                any<HttpEntity<*>>(),
                SnippetCreationResponse::class.java,
            )
        } returns
            ResponseEntity.ok(
                SnippetCreationResponse(true, "yay"),
            )
        val response = appClient.registerSnippet(snippetId, userId, name, language).body!!
        assertEquals(true, response.success)
        assertEquals("yay", response.message)
    }
}
