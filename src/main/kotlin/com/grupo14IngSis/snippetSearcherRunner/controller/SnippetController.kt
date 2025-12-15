package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.GetSnippetResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
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
@RequestMapping("/api/v1/snippet")
class SnippetController(
    private val assetServiceClient: AssetServiceClient,
    private val appClient: AppClient,
) {
    /**
     * GET    /api/v1/snippet/{container}/{snippetId}
     *
     * Fetch the content of a snippet as a String
     *
     * Response:
     *     {
     *         name: String,
     *         content: String
     *     }
     */
    @GetMapping("/{container}/{snippetId}")
    fun getSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
    ): ResponseEntity<*> {
        val snippet = appClient.getSnippet(snippetId)
        val content = assetServiceClient.getAsset(container, snippetId)
        if (snippet == null || content == null) {
            return ResponseEntity.status(404).body("Snippet with id $snippetId in container $container not found")
        }
        val output = GetSnippetResponse(snippet.name, content)
        return ResponseEntity.ok().body(output)
    }

    /**
     * PUT    /api/v1/snippet/{container}/{snippetId}
     *
     * Create a snippet
     *
     * Request:
     *
     *     {
     *       userId: String
     *       name: String,
     *       language: String,
     *       Snippet: String
     *     }
     */
    @PutMapping("/{container}/{snippetId}")
    fun putSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<Any> {
        val snippetNotExists = assetServiceClient.getAsset(container, snippetId) == null
        if (snippetNotExists) {
            assetServiceClient.postAsset(container, snippetId, request.snippet)
            appClient.registerSnippet(snippetId, request.userId, request.language)
            return ResponseEntity.created(URI.create("/api/v1/snippet/$container/$snippetId"))
                .body("Snippet created.")
        } else {
            return ResponseEntity.badRequest().body("Error processing snippet.")
        }
    }

    /**
     * PATCH    /api/v1/snippet/{container}/{snippetId}
     *
     * Update the content of a snippet
     *
     * This endpoint overrides the snippet's content with the request data
     *
     * Request:
     *
     *     {
     *       jwt: String,
     *       Snippet: String
     *     }
     */
    @PatchMapping("/{container}/{snippetId}")
    fun patchSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetUpdateRequest,
    ): ResponseEntity<Any> {
        val snippetExists = assetServiceClient.getAsset(container, snippetId) != null
        if (snippetExists) {
            assetServiceClient.postAsset(container, snippetId, request.snippet)
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
     * DELETE /api/v1/snippet/{container}/{snippetId}
     *
     * Delete a snippet from the service
     */
    @DeleteMapping("/{container}/{snippetId}")
    fun deleteSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> {
        val statusCode = assetServiceClient.deleteAsset(container, snippetId)
        return when {
            statusCode in 200..299 -> ResponseEntity.noContent().build() // Use 204 No Content for successful deletion
            statusCode == 404 -> ResponseEntity.status(404).body("Snippet with id $snippetId in container $container not found.")
            else -> ResponseEntity.status(statusCode).body("Error deleting snippet.")
        }
    }
}
