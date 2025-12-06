package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionStatus
import org.example.Runner
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val assetService: AssetServiceClient,
) {
    private val runner = Runner()

    fun executeSnippet(
        snippetId: String,
        version: String?,
    ): ExecutionResponse {
        val snippet =
            assetService.getAsset("snippets", snippetId)
                ?: return ExecutionResponse(
                    ExecutionStatus.ERROR,
                    "Snippet not found",
                )
        // SourceFile version
        val args: MutableList<String?> = mutableListOf(snippet, version)
        val argsSafe = args.filterNotNull()

        // Must give input and catch prints

        runner.executionCommand(argsSafe)

        // Must check if the execution succeeded

        return ExecutionResponse(
            ExecutionStatus.FINISHED,
            "Execution finished",
        )
    }
}
