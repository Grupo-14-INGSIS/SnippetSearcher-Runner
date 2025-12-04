package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
) {
    /**
     * GET    /api/v1/snippet/{container}/{snippetId}
     *
     * Fetch the content of a snippet as a String
     */
    @GetMapping("/{container}/{snippetId}")
    fun getSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
    ): ResponseEntity<String> {
        return assetServiceClient.getAsset(container, snippetId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(404).body("Snippet with id $snippetId in container $container not found")
    }

    /**
     * PUT    /api/v1/snippet/{container}/{snippetId}
     *
     * Create or update the content of a snippet. The body is a String
     */
    @PutMapping("/{container}/{snippetId}")
    fun putSnippet(
        @PathVariable container: String,
        @PathVariable snippetId: String,
        @RequestBody snippet: String,
    ): ResponseEntity<Any> {
        val statusCode = assetServiceClient.postAsset(container, snippetId, snippet)
        return when (statusCode) {
            200 -> ResponseEntity.ok().body("Snippet updated.")
            201 -> ResponseEntity.created(URI.create("/api/v1/snippet/$container/$snippetId")).body("Snippet created.")
            else -> ResponseEntity.status(statusCode).body("Error processing snippet.")
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
