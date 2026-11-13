package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutionServiceTest {
    private lateinit var executionService: ExecutionService
    private lateinit var assetServiceClient: AssetServiceClient

    @BeforeEach
    fun setup() {
        assetServiceClient = mockk(relaxed = true)
        progress = mockk(relaxed = true)
        every { progress.key(any(), any(), any()) } answers
            { "execution:${firstArg<String>()}:${secondArg<String>()}:${thirdArg<String>()}" }
        executionService = ExecutionService(SnippetStorage(assetServiceClient), progress)
    }

    private lateinit var progress: ExecutionProgressStore

    @Test
    fun `each printed line is published as it happens and the final status at the end`() {
        every { assetServiceClient.getAsset("snippets", "snippet-1") } returns "println(\"uno\");\nprintln(\"dos\");"

        val output = executionService.executeSnippet("snippet-1", "u1", "1.0", emptyMap())

        assertEquals(listOf("uno", "dos", "Execution finished"), output.message)
        verifyOrder {
            progress.start("execution:run:u1:snippet-1")
            progress.append("execution:run:u1:snippet-1", "uno")
            progress.append("execution:run:u1:snippet-1", "dos")
            progress.finish("execution:run:u1:snippet-1", ExecutionEventType.COMPLETED)
        }
    }

    @Test
    fun `waiting for input publishes the prompt but not the internal cancellation`() {
        every { assetServiceClient.getAsset("snippets", "snippet-1") } returns
            "println(\"antes\");\nlet n: string = readInput(\"name?\");\nprintln(n);"

        val output = executionService.executeSnippet("snippet-1", "u1", "1.1", emptyMap())

        assertEquals(ExecutionEventType.WAITING, output.status)
        verify { progress.append("execution:run:u1:snippet-1", "antes") }
        verify { progress.append("execution:run:u1:snippet-1", "name?") }
        verify { progress.finish("execution:run:u1:snippet-1", ExecutionEventType.WAITING) }
        Thread.sleep(200)
        verify(exactly = 0) { progress.append("execution:run:u1:snippet-1", "Execution canceled") }
    }

    @Test
    fun `status is read from Redis so any replica can answer it`() {
        every { progress.get("execution:test:u1:snippet-1") } returns
            com.grupo14IngSis.snippetSearcherRunner.dto
                .ExecutionResponse(ExecutionEventType.OUTPUT, listOf("uno"))

        val status = executionService.getExecutionStatus("snippet-1", "u1", ExecutionProgressStore.TEST)

        assertEquals(ExecutionEventType.OUTPUT, status.status)
        assertEquals(listOf("uno"), status.message)
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
        assertEquals(ExecutionEventType.COMPLETED, output.status)
        assertEquals(listOf("Hello, World!", "Execution finished"), output.message)
    }
}
