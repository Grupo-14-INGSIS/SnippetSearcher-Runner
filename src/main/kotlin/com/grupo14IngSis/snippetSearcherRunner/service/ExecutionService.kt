package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val assetServiceClient: AssetServiceClient,
    private val appClient: AppClient,
) {
    private val activeExecutions: MutableMap<String, SnippetExecution> = mutableMapOf()

    private val maxConcurrentExecutions = 14

    fun executeSnippet(
        snippetId: String,
        userId: String,
        version: String,
        environment: Map<String, String>,
    ): ExecutionResponse {
        if (activeExecutions.size >= maxConcurrentExecutions) {
            return ExecutionResponse(
                ExecutionEventType.ERROR,
                "Maximum concurrent executions reached",
            )
        }

        val executionId = userId + snippetId

        if (activeExecutions.containsKey(executionId)) {
            val existing = activeExecutions[executionId]
            if (existing?.isRunning() == true) {
                return ExecutionResponse(ExecutionEventType.ERROR, "Execution already running")
            }
            activeExecutions.remove(executionId)
        }

        val execution =
            SnippetExecution(
                snippetId,
                userId,
                version,
                environment,
                appClient,
                assetServiceClient,
            )

        activeExecutions[executionId] = execution

        try {
            execution.start()
            Thread {
                try {
                    while (execution.isRunning()) {
                        Thread.sleep(500)
                    }
                } finally {
                    activeExecutions.remove(executionId)
                }
            }.apply {
                isDaemon = true
                start()
            }

            return ExecutionResponse(ExecutionEventType.STARTED, "Execution started.")
        } catch (e: Exception) {
            activeExecutions.remove(executionId)
            return ExecutionResponse(ExecutionEventType.ERROR, "Execution error: ${e.message}")
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

    fun enqueueInputs(
        snippetId: String,
        userId: String,
        inputs: List<String>,
    ): Boolean {
        val executionId = userId + snippetId
        val execution = activeExecutions[executionId]
        if (execution != null && execution.isRunning()) {
            execution.sendMultipleInputs(inputs)
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

    fun isExecutionRunning(executionId: String): Boolean {
        val execution = activeExecutions[executionId]
        return execution?.isRunning() ?: false
    }

    fun getActiveExecutionIds(): List<String> {
        return activeExecutions.keys.toList()
    }

    fun cancelAllExecutions() {
        activeExecutions.values.forEach { it.cancel() }
        activeExecutions.clear()
    }
}
