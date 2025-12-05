package com.grupo14IngSis.snippetSearcherRunner.plugins

import io.mockk.mockk
import io.mockk.verify
import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AnalyzerPluginTest {

    private lateinit var runner: Runner
    private lateinit var analyzerPlugin: AnalyzerPlugin

    @BeforeEach
    fun setUp() {
        runner = mockk(relaxed = true)
        analyzerPlugin = AnalyzerPlugin(runner)
    }

    @Test
    fun `run with null snippet should return empty string`() {
        val result = analyzerPlugin.run(null, emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run with blank snippet should return empty string`() {
        val result = analyzerPlugin.run("   ", emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run without configFileContent should throw IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            analyzerPlugin.run("some snippet", emptyMap())
        }
        assertEquals("Configuration file content 'configFileContent' is required for analysis.", exception.message)
    }

    @Test
    fun `run with valid snippet and config should call analyzerCommand`() {
        val snippet = "println(\"hello\");"
        val configFileContent = "rules: []"
        val params = mapOf("configFileContent" to configFileContent, "version" to "1.1")

        analyzerPlugin.run(snippet, params)

        verify { runner.analyzerCommand(any()) }
    }
    
    @Test
    fun `run with valid snippet and config should call analyzerCommand with specific version`() {
        val snippet = "println(\"hello\");"
        val configFileContent = "rules: []"
        val version = "1.1"
        val params = mapOf("configFileContent" to configFileContent, "version" to version)

        analyzerPlugin.run(snippet, params)

        verify { runner.analyzerCommand(match { it.contains(version) }) }
    }
    
    @Test
    fun `run with valid snippet and config but no version should call analyzerCommand without version`() {
        val snippet = "println(\"hello\");"
        val configFileContent = "rules: []"
        val params = mapOf("configFileContent" to configFileContent)

        analyzerPlugin.run(snippet, params)

        verify { runner.analyzerCommand(match { !it.contains("1.0") && it.size == 2 }) }
    }
}