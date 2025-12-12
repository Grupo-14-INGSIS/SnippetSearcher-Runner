package com.grupo14IngSis.snippetSearcherRunner.plugins

import io.mockk.mockk
import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class ExecutionPluginTest {
    private lateinit var runner: Runner
    private lateinit var executionPlugin: ExecutionPlugin

    @BeforeEach
    fun setUp() {
        runner = mockk(relaxed = true)
        executionPlugin = ExecutionPlugin()
    }

    @Test
    fun `run with null snippet should return empty string`() {
        val result = executionPlugin.run(null, emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run with blank snippet should return empty string`() {
        val result = executionPlugin.run("   ", emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run with valid snippet should return print`() {
        val snippet = "println(\"hello\");"
        val output = executionPlugin.run(snippet, emptyMap()) as String
        assertContains(output, "hello")
    }

    @Test
    fun `run with complex snippet should return print`() {
        val snippet =
            "println(\"Hello, World!\");\n" +
                "println(\"This is a long snippet.\");\n" +
                "println(\"One with many lines!\");\n" +
                "println(\"Goodbye, World!\");"
        val output = executionPlugin.run(snippet, emptyMap()) as String
        val expectedLines =
            listOf(
                "Hello, World!",
                "This is a long snippet.",
                "One with many lines!",
                "Goodbye, World!",
            )

        for (line in expectedLines) {
            assertContains(output, line)
        }
    }
}
