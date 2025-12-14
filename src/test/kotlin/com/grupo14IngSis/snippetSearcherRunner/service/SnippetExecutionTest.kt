package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import inputprovider.src.main.kotlin.ConsoleInputProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import runner.src.main.kotlin.Runner

class SnippetExecutionTest {
    private lateinit var execution: SnippetExecution
    private lateinit var appClient: AppClient
    private lateinit var assetService: AssetServiceClient

    @BeforeEach
    fun setUp() {
        assetService = mockk(relaxed = true)
        appClient = mockk(relaxed = true)
        execution =
            SnippetExecution(
                "snippetId",
                "userId",
                "1.1",
                mapOf(),
                appClient,
                assetService,
            )
    }

    @Test
    fun `run with null snippet should return empty string`() {
        every { assetService.getAsset("snippets", "snippetId") } returns null
        every { appClient.sendLine(any(), any(), any(), any()) } returns Unit
        execution.start()
        while (execution.isRunning()) continue
        val result = execution.getOutput()
        Assertions.assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `run with blank snippet should return empty string`() {
        every { assetService.getAsset("snippets", "snippetId") } returns ""
        every { appClient.sendLine(any(), any(), any(), any()) } returns Unit
        execution.start()
        while (execution.isRunning()) continue
        val result = execution.getOutput()
        Assertions.assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `run with valid snippet should return print`() {
        every { assetService.getAsset("snippets", "snippetId") } returns "println(\"Hello, World!\");"
        every { appClient.sendLine(any(), any(), any(), any()) } returns Unit
        execution.start()
        while (execution.isRunning()) {
            Thread.sleep(10) // ← Agrega esto
        }
        Thread.sleep(100) // ← Y esto para dar margen
        val result = execution.getOutput()
        Assertions.assertEquals(listOf("Hello, World!"), result)
    }

    @Test
    fun `run with complex snippet should return print`() {
        val snippet =
            "println(\"Hello, World!\");\n" +
                "println(\"This is a long snippet.\");\n" +
                "println(\"One with many lines!\");\n" +
                "println(\"Goodbye, World!\");"
        val expectedLines =
            listOf(
                "Hello, World!",
                "This is a long snippet.",
                "One with many lines!",
                "Goodbye, World!",
            )

        every { assetService.getAsset("snippets", "snippetId") } returns snippet
        every { appClient.sendLine(any(), any(), any(), any()) } returns Unit
        execution.start()
        while (execution.isRunning()) continue
        val result = execution.getOutput()
        Assertions.assertEquals(expectedLines, result)
    }

    @Test
    fun `test runner printer directly`() {
        val tempFile = createTempFile("test", ".ps")
        tempFile.writeText("println(\"Direct test\");")

        val capturedOutputs = mutableListOf<String>()
        val runner = Runner() // Runner REAL

        println("🚀 Starting direct test...")
        runner.executionCommand(
            listOf(tempFile.absolutePath, "1.0"),
            ConsoleInputProvider(),
            printer = { output ->
                println("✅ PRINTER CALLED: $output")
                capturedOutputs.add(output.toString())
            },
        )

        println("📊 Captured ${capturedOutputs.size} outputs:")
        capturedOutputs.forEach { println("  - '$it'") }

        tempFile.delete()

        Assertions.assertTrue(capturedOutputs.isNotEmpty(), "Should capture at least one output")
    }
}
