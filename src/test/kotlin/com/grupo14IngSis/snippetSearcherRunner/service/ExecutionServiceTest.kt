package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ExecutionServiceTest {
    private lateinit var executionService: ExecutionService
    private lateinit var assetServiceClient: AssetServiceClient

    @BeforeEach
    fun setup() {
        assetServiceClient = mockk(relaxed = true)
        executionService = ExecutionService(assetServiceClient)
    }

    @Test
    fun `Should return snippet execution`() {
        val snippetId = "123"
        val snippet = "println(\"Hello, World\");"
        every { assetServiceClient.getAsset("snippets", "123") } returns snippet
        val output = executionService.executeSnippet(snippetId, null).message as String

        assertContains(output, "Hello, World")
    }

    @Test
    fun `Should return error with invalid snippet ID`() {
        val snippetId = ""
        every { assetServiceClient.getAsset("snippets", "") } returns null
        val output = executionService.executeSnippet(snippetId, null)

        assertEquals(ExecutionEventType.ERROR, output.type)
        assertEquals("Snippet not found", output.message)
    }
}
