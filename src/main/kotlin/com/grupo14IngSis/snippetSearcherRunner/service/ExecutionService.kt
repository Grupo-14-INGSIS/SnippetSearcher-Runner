package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ExecutionService(
    private val assetServiceClient: AssetServiceClient,
    private val snippetCacheService: SnippetCacheService,
) {
    private val activeExecutions: MutableMap<String, SnippetExecution> = ConcurrentHashMap()

    private val maxConcurrentExecutions = 1400

    /** Máximo tiempo que esperamos a que un snippet termine (o pida input) antes de abortarlo. */
    private val maxExecutionMillis = 120_000L

    private val pollMillis = 5L

    /**
     * Ejecuta un snippet con los inputs ya provistos. Bloquea hasta que la ejecución termina, falla
     * o se queda esperando un input que todavía no fue provisto (`readInput`).
     *
     * En ese último caso devuelve `WAITING` con el output acumulado y descarta la ejecución: el
     * cliente debe volver a llamar con la lista de inputs ampliada. Como el snippet es determinístico
     * el output se reproduce, y el Runner no guarda estado entre requests (funciona con N réplicas).
     */
    fun executeSnippet(
        snippetId: String,
        userId: String,
        version: String,
        environment: Map<String, String>,
        inputs: List<String> = emptyList(),
    ): ExecutionResponse {
        val cacheKey = "snippet:$snippetId:$version"
        // Always execute snippet fresh to avoid returning stale cached errors

        if (activeExecutions.size >= maxConcurrentExecutions) {
            return ExecutionResponse(
                ExecutionEventType.ERROR,
                listOf("Maximum concurrent executions reached"),
            )
        }

        val executionId = executionId(userId, snippetId)

        val existing = activeExecutions[executionId]
        if (existing != null) {
            if (existing.isRunning()) {
                return ExecutionResponse(ExecutionEventType.ERROR, listOf("Execution already running"))
            }
            activeExecutions.remove(executionId)
        }

        val execution =
            SnippetExecution(
                snippetId,
                version,
                environment,
                assetServiceClient,
            )

        activeExecutions[executionId] = execution

        try {
            execution.sendMultipleInputs(inputs)
            execution.start()
            waitUntilIdle(execution, maxExecutionMillis)

            if (execution.isWaitingForInput()) {
                val output = execution.getOutput()
                execution.cancel()
                activeExecutions.remove(executionId)
                return ExecutionResponse(ExecutionEventType.WAITING, output)
            }
            if (execution.isRunning()) {
                val output = execution.getOutput()
                execution.cancel()
                activeExecutions.remove(executionId)
                return ExecutionResponse(ExecutionEventType.ERROR, output + "Execution timed out")
            }

            val output = execution.getOutput()
            activeExecutions.remove(executionId)
            val response = ExecutionResponse(finalStatus(execution), output)
            // Assuming output is a list of strings that can be joined to a single string for caching
            snippetCacheService.saveToCache(cacheKey, output.joinToString("\n"))
            return response
        } catch (e: Exception) {
            activeExecutions.remove(executionId)
            return ExecutionResponse(ExecutionEventType.ERROR, listOf("Execution error: ${e.message}"))
        }
    }

    fun sendInput(
        snippetId: String,
        userId: String,
        input: String,
    ): Boolean {
        val executionId = executionId(userId, snippetId)
        val execution = activeExecutions[executionId]
        if (execution != null && execution.isRunning()) {
            execution.sendInput(input)
            return true
        }
        return false
    }

    fun cancelExecution(
        snippetId: String,
        userId: String,
    ): Boolean {
        val executionId = executionId(userId, snippetId)
        val execution = activeExecutions[executionId]
        if (execution != null && execution.isRunning()) {
            execution.cancel()
            activeExecutions.remove(executionId)
            return true
        }
        return false
    }

    /**
     * Estado actual de una ejecución en curso (si la hay en esta instancia).
     */
    fun getExecutionStatus(
        snippetId: String,
        userId: String?,
    ): ExecutionResponse {
        val execution =
            if (userId != null) {
                activeExecutions[executionId(userId, snippetId)]
            } else {
                activeExecutions.entries.firstOrNull { it.key.endsWith(snippetId) }?.value
            } ?: return ExecutionResponse(ExecutionEventType.COMPLETED, emptyList())

        return when {
            execution.isWaitingForInput() -> ExecutionResponse(ExecutionEventType.WAITING, execution.getOutput())
            execution.isRunning() -> ExecutionResponse(ExecutionEventType.OUTPUT, execution.getOutput())
            else -> ExecutionResponse(finalStatus(execution), execution.getOutput())
        }
    }

    private fun executionId(
        userId: String,
        snippetId: String,
    ): String = userId + snippetId

    private fun finalStatus(execution: SnippetExecution): ExecutionEventType =
        when (execution.getStatus()) {
            ExecutionEventType.ERROR -> ExecutionEventType.ERROR
            ExecutionEventType.CANCELLED -> ExecutionEventType.CANCELLED
            else -> ExecutionEventType.COMPLETED
        }

    /**
     * Espera hasta que la ejecución termine o quede bloqueada esperando input (o se agote el tiempo).
     */
    private fun waitUntilIdle(
        execution: SnippetExecution,
        timeoutMillis: Long,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (execution.isRunning() && !execution.isWaitingForInput() && System.currentTimeMillis() < deadline) {
            Thread.sleep(pollMillis)
        }
    }
}
