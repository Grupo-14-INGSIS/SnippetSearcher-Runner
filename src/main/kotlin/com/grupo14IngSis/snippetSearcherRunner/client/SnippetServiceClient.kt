package com.grupo14IngSis.snippetSearcherRunner.client

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
/**
 * DTO que representa un snippet del servicio externo
 */
data class SnippetDto(
    val id: String,
    val userId: String,
    val content: String,
    val language: String,
    val name: String,
)

/**
 * DTO para test cases
 */
data class TestCaseDto(
    val id: String,
    val name: String,
    val input: List<String>,
    val expectedOutput: List<String>,
)

/**
 * DTO para respuesta de ejecución
 */
data class ExecutionResponse(
    val output: List<String>,
    val errors: List<String>?,
)


@Service
class SnippetServiceClient(
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val snippetServiceUrl =
        System.getenv("SNIPPET_SERVICE_URL")
            ?: "http://localhost:8081"

    init {
        logger.info("SnippetServiceClient initialized with URL: $snippetServiceUrl")
    }

    fun getSnippetsByUserId(userId: String): List<SnippetDto> {
        logger.info("Fetching all snippets for user: $userId")
        val requestId = MDC.get("request_id")

        return try {
            val snippets =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono<List<SnippetDto>>()
                    .block() ?: emptyList()

            logger.info("Successfully fetched ${snippets.size} snippets for user $userId")
            snippets
        } catch (e: Exception) {
            logger.error("Error fetching snippets for user $userId from $snippetServiceUrl", e)
            emptyList()
        }
    }

    fun countSnippetsByUserId(userId: Int): Int {
        logger.debug("Counting snippets for user: $userId")
        val requestId = MDC.get("request_id")

        return try {
            val count =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId/count")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono<Int>()
                    .block() ?: 0

            logger.debug("User $userId has $count snippets")
            count
        } catch (e: Exception) {
            logger.error("Error counting snippets for user $userId", e)
            0
        }
    }

    fun getSnippetsAfter(
        userId: String,
        afterSnippetId: String,
    ): List<SnippetDto> {
        logger.info("Fetching snippets for user $userId after snippet $afterSnippetId")
        val requestId = MDC.get("request_id")

        return try {
            val snippets =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId/after/$afterSnippetId")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono<List<SnippetDto>>()
                    .block() ?: emptyList()

            logger.info("Fetched ${snippets.size} remaining snippets for user $userId")
            snippets
        } catch (e: Exception) {
            logger.error("Error fetching snippets after $afterSnippetId for user $userId", e)
            emptyList()
        }
    }

    fun updateSnippetContent(
        snippetId: String,
        content: String,
    ): Boolean {
        logger.debug("Updating content for snippet: $snippetId")
        val requestId = MDC.get("request_id")

        return try {
            webClient.put()
                .uri("$snippetServiceUrl/api/snippets/$snippetId/content")
                .header("X-Request-ID", requestId)
                .bodyValue(mapOf("content" to content))
                .retrieve()
                .toBodilessEntity()
                .block()

            logger.debug("Successfully updated snippet $snippetId")
            true
        } catch (e: Exception) {
            logger.error("Error updating snippet $snippetId", e)
            false
        }
    }

    fun isServiceAvailable(): Boolean {
        val requestId = MDC.get("request_id")

        return try {
            webClient.get()
                .uri("$snippetServiceUrl/actuator/health")
                .header("X-Request-ID", requestId)
                .retrieve()
                .toBodilessEntity()
                .block()
            true
        } catch (e: Exception) {
            logger.warn("Snippet Service is not available at $snippetServiceUrl")
            false
        }
    }

    fun getTestCases(snippetId: String): List<TestCaseDto> {
        logger.debug("Fetching test cases for snippet: $snippetId")
        val requestId = MDC.get("request_id")

        return try {
            val testCases =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/$snippetId/test-cases")
                    .header("X-Request-ID", requestId)
                    .retrieve()
                    .bodyToMono<List<TestCaseDto>>()
                    .block() ?: emptyList()

            logger.info("Fetched ${testCases.size} test cases for snippet $snippetId")
            testCases
        } catch (e: Exception) {
            logger.error("Error fetching test cases for snippet $snippetId", e)
            emptyList()
        }
    }

    fun executeSnippet(
        content: String,
        language: String,
        version: String,
        inputs: List<String>,
    ): List<String> {
        logger.debug("Executing snippet via PrintScript")
        val requestId = MDC.get("request_id")

        return try {
            val request =
                mapOf(
                    "content" to content,
                    "language" to language,
                    "version" to version,
                    "inputs" to inputs,
                )

            val response =
                webClient.post()
                    .uri("$snippetServiceUrl/api/snippets/execute")
                    .header("X-Request-ID", requestId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono<ExecutionResponse>()
                    .block()

            response?.output ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error executing snippet", e)
            throw RuntimeException("Execution failed: ${e.message}")
        }
    }
}