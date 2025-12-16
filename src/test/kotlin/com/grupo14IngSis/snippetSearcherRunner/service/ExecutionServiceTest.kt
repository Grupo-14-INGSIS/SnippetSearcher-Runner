package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutionServiceTest {
    private lateinit var executionService: ExecutionService
    private lateinit var assetServiceClient: AssetServiceClient
    private lateinit var snippetCacheService: SnippetCacheService
    private lateinit var appClient: AppClient

    @BeforeEach
    fun setup() {
        assetServiceClient = mockk(relaxed = true)
        appClient = mockk(relaxed = true)
        snippetCacheService = mockk(relaxed = true)
        executionService = ExecutionService(assetServiceClient, snippetCacheService)
    }

    @Test
    fun `Should return snippet output`() {
        val snippetId = "123"
        val userId = "user"
        val snippet = "println(\"Hello, World!\");"
        every { assetServiceClient.getAsset("snippets", snippetId) } returns snippet
        every { snippetCacheService.getFromCache(any()) } returns null
        val output =
            executionService.executeSnippet(
                snippetId,
                userId,
                "1.0",
                emptyMap(),
            )
        assertEquals(ExecutionEventType.COMPLETED, output.status)
        assertEquals(listOf("Hello, World!", "Execution finished"), output.message)
    }

    @Test
    fun `should return cached result if available`() {
        val snippetId = "123"
        val userId = "user"
        val version = "1.0"
        val cachedOutput = "Hello from cache"
        val cacheKey = "snippet:$snippetId:$version"

        every { snippetCacheService.getFromCache(cacheKey) } returns cachedOutput

        val output = executionService.executeSnippet(snippetId, userId, version, emptyMap())

        assertEquals(ExecutionEventType.COMPLETED, output.status)
        assertEquals(listOf(cachedOutput), output.message)
    }

    @Test
    fun `should return error on execution exception`() {
        val snippetId = "456"
        val userId = "user"
        val version = "1.0"
        val snippet = "println(\"This will fail\");"

        every { assetServiceClient.getAsset("snippets", snippetId) } returns snippet
        every { snippetCacheService.getFromCache(any()) } returns null
        every { snippetCacheService.saveToCache(any(), any()) } throws RuntimeException("Cache unavailable")

        val output = executionService.executeSnippet(snippetId, userId, version, emptyMap())

        assertEquals(ExecutionEventType.ERROR, output.status)
        assertEquals(listOf("Execution error: Cache unavailable"), output.message)
    }
}
