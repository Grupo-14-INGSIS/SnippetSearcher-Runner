package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEvent
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.plugins.ExecutionPlugin
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val assetService: AssetServiceClient,
) {
    private val runner = ExecutionPlugin()

    fun executeSnippet(
        snippetId: String,
        version: String?,
    ): ExecutionEvent {
        val snippet =
            assetService.getAsset("snippets", snippetId)
                ?: return ExecutionEvent(
                    ExecutionEventType.ERROR,
                    "Snippet not found",
                )
        val args: MutableMap<String, Any> = mutableMapOf()
        if (version != null) {
            args["version"] = version
        }

        // Must receive input

        val result = runner.run(snippet, args)

        return ExecutionEvent(
            ExecutionEventType.COMPLETED,
            result as String,
        )
    }
}
