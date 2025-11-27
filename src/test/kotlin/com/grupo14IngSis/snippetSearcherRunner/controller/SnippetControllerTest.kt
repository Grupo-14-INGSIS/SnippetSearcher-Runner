package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

class SnippetControllerTest {
    private val assetServiceClient: AssetServiceClient = mockk()
    private val snippetController = SnippetController(assetServiceClient)

    @Test
    fun `getSnippet should return snippet content when asset exists`() {
        val container = "test-container"
        val snippetId = "test-snippet"
        val snippetContent = "Hello, world!"
        every { assetServiceClient.getAsset(container, snippetId) } returns snippetContent

        val response: ResponseEntity<String> = snippetController.getSnippet(container, snippetId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippetContent, response.body)
    }

    @Test
    fun `getSnippet should return 404 when asset does not exist`() {
        val container = "test-container"
        val snippetId = "non-existent-snippet"
        every { assetServiceClient.getAsset(container, snippetId) } returns null

        val response: ResponseEntity<String> = snippetController.getSnippet(container, snippetId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Snippet with id $snippetId in container $container not found", response.body)
    }

    @Test
    fun `putSnippet should return 200 when updating an existing snippet`() {
        val container = "test-container"
        val snippetId = "test-snippet"
        val snippetContent = "Updated content"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 200

        val response: ResponseEntity<Any> = snippetController.putSnippet(container, snippetId, snippetContent)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Snippet updated.", response.body)
    }

    @Test
    fun `putSnippet should return 201 when creating a new snippet`() {
        val container = "test-container"
        val snippetId = "new-snippet"
        val snippetContent = "New content"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 201

        val response: ResponseEntity<Any> = snippetController.putSnippet(container, snippetId, snippetContent)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Snippet created.", response.body)
        assertEquals(URI.create("/api/v1/snippet/$container/$snippetId"), response.headers.location)
    }

    @Test
    fun `putSnippet should return error status when processing fails`() {
        val container = "test-container"
        val snippetId = "error-snippet"
        val snippetContent = "Error content"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 500

        val response: ResponseEntity<Any> = snippetController.putSnippet(container, snippetId, snippetContent)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Error processing snippet.", response.body)
    }

    @Test
    fun `deleteSnippet should return 204 when snippet is deleted successfully`() {
        val container = "test-container"
        val snippetId = "test-snippet"
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 204

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(container, snippetId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `deleteSnippet should return 404 when snippet to delete is not found`() {
        val container = "test-container"
        val snippetId = "non-existent-snippet"
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 404

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(container, snippetId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Snippet with id $snippetId in container $container not found.", response.body)
    }

    @Test
    fun `deleteSnippet should return error status when deletion fails`() {
        val container = "test-container"
        val snippetId = "error-snippet"
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 500

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(container, snippetId)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Error deleting snippet.", response.body)
    }
}
