package com.grupo14IngSis.snippetSearcherRunner.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlin.collections.*
import kotlinx.coroutines.*


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

/**
 * Cliente HTTP para comunicarse con el Snippet Service
 * Este servicio maneja todos los snippets del sistema
 */
@Service
class SnippetServiceClient(
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // URL del servicio de snippets (configurable via variable de entorno)
    private val snippetServiceUrl =
        System.getenv("SNIPPET_SERVICE_URL")
            ?: "http://localhost:8081"

    init {
        logger.info("SnippetServiceClient initialized with URL: $snippetServiceUrl")
    }

    /**
     * Obtiene todos los snippets de un usuario
     *
     * @param userId ID del usuario
     * @return Lista de snippets o lista vacía si hay error
     */
    fun getSnippetsByUserId(userId: String): List<SnippetDto> {
        logger.info("Fetching all snippets for user: $userId")

        return try {
            val snippets =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId")
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

    /**
     * Cuenta el número total de snippets de un usuario
     *
     * @param userId ID del usuario
     * @return Número de snippets o 0 si hay error
     */
    fun countSnippetsByUserId(userId: Int): Int {
        logger.debug("Counting snippets for user: $userId")

        return try {
            val count =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId/count")
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

    /**
     * Obtiene snippets después de un ID específico (para reanudar procesamiento)
     * Útil cuando un job falla y necesita continuar desde donde se quedó
     *
     * @param userId ID del usuario
     * @param afterSnippetId ID del último snippet procesado
     * @return Lista de snippets restantes
     */
    fun getSnippetsAfter(
        userId: String,
        afterSnippetId: String,
    ): List<SnippetDto> {
        logger.info("Fetching snippets for user $userId after snippet $afterSnippetId")

        return try {
            val snippets =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/user/$userId/after/$afterSnippetId")
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

    /**
     * Actualiza el contenido de un snippet (después de formatearlo)
     *
     * @param snippetId ID del snippet a actualizar
     * @param content Nuevo contenido formateado
     * @return true si se actualizó correctamente, false si hubo error
     */
    fun updateSnippetContent(
        snippetId: String,
        content: String,
    ): Boolean {
        logger.debug("Updating content for snippet: $snippetId")

        return try {
            webClient.put()
                .uri("$snippetServiceUrl/api/snippets/$snippetId/content")
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

    /**
     * Verifica si el Snippet Service está disponible
     * Útil para health checks
     *
     * @return true si el servicio responde, false si no
     */
    fun isServiceAvailable(): Boolean {
        return try {
            webClient.get()
                .uri("$snippetServiceUrl/actuator/health")
                .retrieve()
                .toBodilessEntity()
                .block()
            true
        } catch (e: Exception) {
            logger.warn("Snippet Service is not available at $snippetServiceUrl")
            false
        }
    }

    /**
     * Obtiene los test cases de un snippet
     *
     * @param snippetId ID del snippet
     * @return Lista de test cases o lista vacía si hay error
     */
    fun getTestCases(snippetId: String): List<TestCaseDto> {
        logger.debug("Fetching test cases for snippet: $snippetId")

        return try {
            val testCases =
                webClient.get()
                    .uri("$snippetServiceUrl/api/snippets/$snippetId/test-cases")
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

    /**
     * Ejecuta un snippet con PrintScript
     *
     * @param content Código del snippet
     * @param language Lenguaje (printscript)
     * @param version Versión (1.0, 1.1)
     * @param inputs Entradas del usuario
     * @return Lista con las salidas de la ejecución
     */
    fun executeSnippet(
        content: String,
        language: String,
        version: String,
        inputs: List<String>,
    ): List<String> {
        logger.debug("Executing snippet via PrintScript")

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
