package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutionServiceTest {
    private lateinit var executionService: ExecutionService
    private lateinit var assetServiceClient: AssetServiceClient
    private lateinit var appClient: AppClient

    @BeforeEach
    fun setup() {
        assetServiceClient = mockk(relaxed = true)
        appClient = mockk(relaxed = true)
        executionService = ExecutionService(assetServiceClient)
    }

    @Test
    fun `Should return snippet output`() {
        val snippetId = "123"
        val userId = "user"
        val snippet = "println(\"Hello, World!\");"
        every { assetServiceClient.getAsset("snippets", snippetId) } returns snippet
        val output =
            executionService.executeSnippet(
                snippetId,
                userId,
                "1.0",
                emptyMap(),
            )
        if (output.status != ExecutionEventType.COMPLETED) {
            assertTrue(false)
        }
        assertEquals(listOf("Hello, World!", "Execution finished"), output.message)
        assertEquals(ExecutionEventType.COMPLETED, output.status)
    }
}
