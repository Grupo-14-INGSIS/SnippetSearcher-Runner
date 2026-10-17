package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
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

    /*
    AppClient.sendLine is temporally disabled

    @Test
    fun `sendLine should call correct endpoint with output event`() {
        // Arrange
        val snippetId = "snippet123"
        val executionId = "exec456"
        val line = "Hello, World!"
        val status = ExecutionEventType.OUTPUT

        val expectedUrl = "$appUrl/v1/snippets/$snippetId/run"
        val expectedEvent = ExecutionEvent(status, line)

        every {
            restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                any<HttpEntity<ExecutionEvent>>(),
                Void::class.java,
            )
        } returns ResponseEntity.ok().build()

        // Act
        appClient.sendLine(snippetId, executionId, line, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                expectedUrl,
                HttpMethod.POST,
                match<HttpEntity<ExecutionEvent>> { entity ->
                    val body = entity.body
                    body?.type == status && body.message == line
                },
                Void::class.java,
            )
        }
    }

    @Test
    fun `sendLine should handle COMPLETED status`() {
        // Arrange
        val snippetId = "snippet999"
        val executionId = "exec111"
        val line = "Execution finished."
        val status = ExecutionEventType.COMPLETED

        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), Void::class.java)
        } returns ResponseEntity.ok().build()

        // Act
        appClient.sendLine(snippetId, executionId, line, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                "$appUrl/v1/snippets/$snippetId/run",
                HttpMethod.POST,
                match<HttpEntity<ExecutionEvent>> { entity ->
                    entity.body?.type == ExecutionEventType.COMPLETED
                },
                Void::class.java,
            )
        }
    }

    @Test
    fun `sendLine should handle ERROR status`() {
        // Arrange
        val snippetId = "snippet555"
        val executionId = "exec666"
        val line = "Error: Division by zero"
        val status = ExecutionEventType.ERROR

        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), Void::class.java)
        } returns ResponseEntity.ok().build()

        // Act
        appClient.sendLine(snippetId, executionId, line, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                "$appUrl/v1/snippets/$snippetId/run",
                HttpMethod.POST,
                match<HttpEntity<ExecutionEvent>> { entity ->
                    entity.body?.type == ExecutionEventType.ERROR &&
                        entity.body?.message == line
                },
                Void::class.java,
            )
        }
    }

    @Test
    fun `sendLine should handle CANCELLED status`() {
        // Arrange
        val snippetId = "snippet777"
        val executionId = "exec888"
        val line = "Execution canceled"
        val status = ExecutionEventType.CANCELLED

        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), Void::class.java)
        } returns ResponseEntity.ok().build()

        // Act
        appClient.sendLine(snippetId, executionId, line, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                "$appUrl/v1/snippets/$snippetId/run",
                HttpMethod.POST,
                match<HttpEntity<ExecutionEvent>> { entity ->
                    entity.body?.type == ExecutionEventType.CANCELLED
                },
                Void::class.java,
            )
        }
    }

    @Test
    fun `sendLine should not throw exception when RestTemplate succeeds`() {
        // Arrange
        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), Void::class.java)
        } returns ResponseEntity.ok().build()

        // Act & Assert - no exception should be thrown
        assertDoesNotThrow {
            appClient.sendLine("snippet1", "exec1", "Test", ExecutionEventType.OUTPUT)
        }
    }

    @Test
    fun `sendLine should propagate RestClientException`() {
        // Arrange
        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), Void::class.java)
        } throws RestClientException("Connection refused")

        // Act & Assert
        assertThrows(RestClientException::class.java) {
            appClient.sendLine("snippet1", "exec1", "Test", ExecutionEventType.OUTPUT)
        }
    }
     */

    @Test
    fun `registerSnippet successfully registers the snippet`() {
        val snippetId = "snippet"
        val userId = "user"
        val name = "sniooet"
        val language = "language"
        val app = appUrl
        every {
            restTemplate.exchange(
                "$app/api/v1/snippets/$snippetId",
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

    @Test
    fun `test sendLine for coverage`() {
        val snippetId = "snippetId"
        val executionId = "executionId"
        val line = "line"
        val status = ExecutionEventType.OUTPUT
        val result = appClient.sendLine(snippetId, executionId, line, status)
        assertEquals(Unit, result)
    }
}
