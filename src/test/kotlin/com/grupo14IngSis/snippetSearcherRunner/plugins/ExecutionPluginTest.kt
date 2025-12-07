package com.grupo14IngSis.snippetSearcherRunner.plugins

import io.mockk.mockk
import io.mockk.verify
import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `run with valid snippet should call executionCommand`() {
        val snippet = "println(\"hello\");"
        executionPlugin.run(snippet, emptyMap())

        verify { runner.executionCommand(any()) }
    }

    @Test
    fun `run with specific version should call executionCommand with that version`() {
        val snippet = "println(\"hello\");"
        val version = "1.1"
        val params = mapOf("version" to version)

        executionPlugin.run(snippet, params)

        verify { runner.executionCommand(match { it.contains(version) }) }
    }

    @Test
    fun `run with no version should call executionCommand with default version`() {
        val snippet = "println(\"hello\");"

        executionPlugin.run(snippet, emptyMap())

        verify { runner.executionCommand(match { it.contains("1.0") }) }
    }
}
