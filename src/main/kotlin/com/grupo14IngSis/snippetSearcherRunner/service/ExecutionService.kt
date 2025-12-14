package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val assetServiceClient: AssetServiceClient,
) {
    private val activeExecutions: MutableMap<String, SnippetExecution> = mutableMapOf()

    private val maxConcurrentExecutions = 1400

    fun executeSnippet(
        snippetId: String,
        userId: String,
        version: String,
        environment: Map<String, String>,
    ): ExecutionResponse {
        if (activeExecutions.size >= maxConcurrentExecutions) {
            return ExecutionResponse(
                ExecutionEventType.ERROR,
                listOf("Maximum concurrent executions reached"),
            )
        }

        val executionId = userId + snippetId

        if (activeExecutions.containsKey(executionId)) {
            val existing = activeExecutions[executionId]
            if (existing?.isRunning() == true) {
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
            execution.start()
            while (execution.isRunning()) continue
            val output = execution.getOutput()
            activeExecutions.remove(executionId)
            return ExecutionResponse(ExecutionEventType.COMPLETED, output)
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
        val executionId = userId + snippetId
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
        val executionId = userId + snippetId
        val execution = activeExecutions[executionId]
        if (execution != null && execution.isRunning()) {
            execution.cancel()
            activeExecutions.remove(executionId)
            return true
        }
        return false
    }
}
