package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEvent
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetStatusUpdateRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
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
    fun `updateSnippetTaskStatus should call correct endpoint with correct body`() {
        // Arrange
        val snippetId = "snippet123"
        val userId = "user456"
        val task = "format"
        val status = true

        val expectedUrl = "$appUrl/api/v1/snippets/$snippetId/status"
        val expectedBody = SnippetStatusUpdateRequest(userId, task, status)

        every {
            restTemplate.exchange(
                expectedUrl,
                HttpMethod.PATCH,
                any<HttpEntity<SnippetStatusUpdateRequest>>(),
                String::class.java,
            )
        } returns ResponseEntity.ok("Success")

        // Act
        appClient.updateSnippetTaskStatus(snippetId, userId, task, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                expectedUrl,
                HttpMethod.PATCH,
                match<HttpEntity<SnippetStatusUpdateRequest>> { entity ->
                    val body = entity.body
                    body?.userId == userId &&
                        body.task == task &&
                        body.status == status
                },
                String::class.java,
            )
        }
    }

    @Test
    fun `updateSnippetTaskStatus should work with status false`() {
        // Arrange
        val snippetId = "snippet789"
        val userId = "user000"
        val task = "lint"
        val status = false

        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), String::class.java)
        } returns ResponseEntity.ok("Success")

        // Act
        appClient.updateSnippetTaskStatus(snippetId, userId, task, status)

        // Assert
        verify(exactly = 1) {
            restTemplate.exchange(
                "$appUrl/api/v1/snippets/$snippetId/status",
                HttpMethod.PATCH,
                any<HttpEntity<SnippetStatusUpdateRequest>>(),
                String::class.java,
            )
        }
    }

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
    fun `updateSnippetTaskStatus should not throw exception when RestTemplate succeeds`() {
        // Arrange
        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), String::class.java)
        } returns ResponseEntity.ok("Success")

        // Act & Assert - no exception should be thrown
        assertDoesNotThrow {
            appClient.updateSnippetTaskStatus("snippet1", "user1", "format", true)
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

    @Test
    fun `updateSnippetTaskStatus should propagate RestClientException`() {
        // Arrange
        every {
            restTemplate.exchange(any<String>(), any(), any<HttpEntity<Any>>(), String::class.java)
        } throws RestClientException("Service unavailable")

        // Act & Assert
        assertThrows(RestClientException::class.java) {
            appClient.updateSnippetTaskStatus("snippet1", "user1", "lint", false)
        }
    }
}
