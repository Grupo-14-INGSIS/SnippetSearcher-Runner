package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.GetSnippetResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/snippets")
class SnippetController(
    private val assetServiceClient: AssetServiceClient,
    private val appClient: AppClient,
) {
    /**
     * GET    /api/v1/snippets/{snippetId}
     *
     * Fetch the content of a snippet as a String
     */
    @GetMapping("/{snippetId}")
    fun getSnippet(
        @PathVariable snippetId: String,
    ): ResponseEntity<*> = getSnippet("snippets", snippetId)

    @GetMapping("/{container}/{snippetId}")
    fun getSnippet(
        @PathVariable container: String?,
        @PathVariable snippetId: String,
    ): ResponseEntity<*> {
        val targetContainer = if (container.isNullOrBlank() || container == snippetId) "snippets" else container
        val snippet = appClient.getSnippet(snippetId)
        val content = assetServiceClient.getAsset(targetContainer, snippetId)
        if (content == null) {
            return ResponseEntity.status(404).body("Snippet with id $snippetId in container $targetContainer not found")
        }
        val output = GetSnippetResponse(snippet?.name ?: "Snippet", content)
        return ResponseEntity.ok().body(output)
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}
     *
     * Create or upload snippet content
     */
    @PutMapping("/{snippetId}")
    fun putSnippet(
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<Any> = putSnippet("snippets", snippetId, request)

    @PutMapping("/{container}/{snippetId}")
    fun putSnippet(
        @PathVariable container: String?,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<Any> {
        val targetContainer = if (container.isNullOrBlank() || container == snippetId) "snippets" else container
        val version = if (!request.version.isNullOrBlank()) request.version else "1.1"
        val validationPlugin = ValidationPlugin()
        val validationResult = validationPlugin.run(request.snippet, mapOf("version" to version)) as String
        val hasError = validationResult.contains("ERROR", ignoreCase = true) ||
            validationResult.contains("Exception", ignoreCase = true) ||
            validationResult.contains("Syntax error", ignoreCase = true) ||
            validationResult.contains("Parsing error", ignoreCase = true)

        if (hasError) {
            return ResponseEntity.badRequest().body(validationResult)
        }

        val snippetNotExists = assetServiceClient.getAsset(targetContainer, snippetId) == null
        if (snippetNotExists) {
            assetServiceClient.postAsset(targetContainer, snippetId, request.snippet)
            appClient.registerSnippet(snippetId, request.userId, request.name, request.language, version, request.description ?: "")
            return ResponseEntity.created(URI.create("/api/v1/snippets/$snippetId"))
                .body("Snippet created.")
        } else {
            return ResponseEntity.badRequest().body("Error processing snippet.")
        }
    }

    /**
     * PATCH    /api/v1/snippets/{snippetId}
     *
     * Update the content of a snippet
     */
    @PatchMapping("/{snippetId}")
    fun patchSnippet(
        @PathVariable snippetId: String,
        @RequestBody request: SnippetUpdateRequest,
    ): ResponseEntity<Any> = patchSnippet("snippets", snippetId, request)

    @PatchMapping("/{container}/{snippetId}")
    fun patchSnippet(
        @PathVariable container: String?,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetUpdateRequest,
    ): ResponseEntity<Any> {
        val targetContainer = if (container.isNullOrBlank() || container == snippetId) "snippets" else container
        val version = request.version ?: "1.1"
        val validationPlugin = ValidationPlugin()
        val validationResult = validationPlugin.run(request.snippet, mapOf("version" to version)) as String
        val hasError = validationResult.contains("ERROR", ignoreCase = true) ||
            validationResult.contains("Exception", ignoreCase = true) ||
            validationResult.contains("Syntax error", ignoreCase = true) ||
            validationResult.contains("Parsing error", ignoreCase = true)

        if (hasError) {
            return ResponseEntity.badRequest().body(validationResult)
        }

        val snippetExists = assetServiceClient.getAsset(targetContainer, snippetId) != null
        if (snippetExists) {
            assetServiceClient.postAsset(targetContainer, snippetId, request.snippet)
            if (request.jwt == null) {
                return ResponseEntity.ok().body("Snippet updated successfully, but could not run tests.")
            }
            val results = appClient.testAll(snippetId, request.jwt)
            var message: String
            if (!results.isEmpty()) {
                message = " with the following test results:\n"
                for (result in results) {
                    message = "$message\n- $result"
                }
            } else {
                message = "."
            }
            return ResponseEntity.ok().body("Snippet updated successfully$message")
        } else {
            return ResponseEntity.badRequest().body("Error processing snippet.")
        }
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}
     *
     * Delete a snippet from the service
     */
    @DeleteMapping("/{snippetId}")
    fun deleteSnippet(
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> = deleteSnippet("snippets", snippetId)

    @DeleteMapping("/{container}/{snippetId}")
    fun deleteSnippet(
        @PathVariable container: String?,
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> {
        val targetContainer = if (container.isNullOrBlank() || container == snippetId) "snippets" else container
        val statusCode = assetServiceClient.deleteAsset(targetContainer, snippetId)
        return when {
            statusCode in 200..299 -> ResponseEntity.noContent().build()
            statusCode == 404 -> ResponseEntity.status(404).body("Snippet with id $snippetId in container $targetContainer not found.")
            else -> ResponseEntity.status(statusCode).body("Error deleting snippet.")
        }
    }

    val tasks =
        mapOf(
            "formatting" to FormattingPlugin(),
            "format" to FormattingPlugin(),
            "linting" to AnalyzerPlugin(),
            "lint" to AnalyzerPlugin(),
        )

    /**
     * PUT /api/v1/snippets/{snippetId}/tasks/{task}
     *
     * Applies a task to a snippet
     */
    @PutMapping(value = ["/{snippetId}/tasks/{task}", "/{snippetId}/task/{task}"])
    fun applyTask(
        @PathVariable snippetId: String,
        @PathVariable task: String,
    ): ResponseEntity<String> {
        val snippet = assetServiceClient.getAsset("snippets", snippetId)
            ?: assetServiceClient.getAsset("snippet", snippetId)
            ?: return ResponseEntity.status(404).body("Snippet not found")
        val plugin = tasks[task.lowercase()] ?: return ResponseEntity.badRequest().body("Unknown task: $task")
        val output = plugin.run(snippet, null) as String
        if (task.lowercase().contains("format")) {
            assetServiceClient.postAsset("snippets", snippetId, output)
            assetServiceClient.postAsset("snippet", snippetId, output)
        }
        return ResponseEntity.ok().body(output)
    }
}
