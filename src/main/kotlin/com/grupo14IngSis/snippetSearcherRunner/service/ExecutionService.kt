package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@Service
class ExecutionService(
    private val storage: SnippetStorage,
    private val snippetCacheService: SnippetCacheService,
    private val progress: ExecutionProgressStore,
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

        // Output en vivo: cada línea va a Redis apenas se imprime y la UI la lee con /status
        val progressKey = progress.key(ExecutionProgressStore.RUN, userId, snippetId)
        progress.start(progressKey)
        // Al cortar la ejecución (queda esperando input) el hilo todavía imprime "Execution canceled":
        // eso no es output del snippet y no tiene que verse en vivo
        val live = AtomicBoolean(true)
        val execution =
            SnippetExecution(
                snippetId,
                version,
                environment,
                storage,
            ) { line, _ -> if (live.get()) progress.append(progressKey, line) }

        activeExecutions[executionId] = execution

        val response =
            try {
                execution.sendMultipleInputs(inputs)
                execution.start()
                waitUntilIdle(execution, maxExecutionMillis)
                if (execution.isRunning()) live.set(false)

                when {
                    execution.isWaitingForInput() -> {
                        val output = execution.getOutput()
                        execution.cancel()
                        ExecutionResponse(ExecutionEventType.WAITING, output)
                    }
                    execution.isRunning() -> {
                        val output = execution.getOutput()
                        execution.cancel()
                        progress.append(progressKey, "Execution timed out")
                        ExecutionResponse(ExecutionEventType.ERROR, output + "Execution timed out")
                    }
                    else -> {
                        val output = execution.getOutput()
                        // Assuming output is a list of strings that can be joined to a single string for caching
                        snippetCacheService.saveToCache(cacheKey, output.joinToString("\n"))
                        ExecutionResponse(finalStatus(execution), output)
                    }
                }
            } catch (e: Exception) {
                ExecutionResponse(ExecutionEventType.ERROR, listOf("Execution error: ${e.message}"))
            } finally {
                activeExecutions.remove(executionId)
            }
        progress.finish(progressKey, response.status)
        return response
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
     * Estado actual de una ejecución (`kind` = run) o de la corrida de un test (`kind` = test), con el
     * output producido hasta ahora. Se lee de Redis: sirve aunque la ejecución corra en otra réplica.
     * Sin userId (o sin datos en Redis) cae a las ejecuciones en memoria de esta instancia.
     */
    fun getExecutionStatus(
        snippetId: String,
        userId: String?,
        kind: String = ExecutionProgressStore.RUN,
    ): ExecutionResponse {
        if (userId != null) {
            progress.get(progress.key(kind, userId, snippetId))?.let { return it }
        }
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
