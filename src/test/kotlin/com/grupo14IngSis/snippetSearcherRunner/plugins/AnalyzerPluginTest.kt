package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AnalyzerPluginTest {
    private lateinit var analyzerPlugin: AnalyzerPlugin

    @BeforeEach
    fun setUp() {
        analyzerPlugin = AnalyzerPlugin()
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
        val exception =
            assertThrows<IllegalArgumentException> {
                analyzerPlugin.run("some snippet", emptyMap())
            }
        assertEquals("Configuration file content 'configFileContent' is required for analysis.", exception.message)
    }

    @Test
    fun `run with valid snippet and empty config should return same snippet`() {
        val snippet = "println(\"hello\");"
        val configFileContent = "rules: []"
        val params = mapOf("configFileContent" to configFileContent, "version" to "1.1")
        val output = analyzerPlugin.run(snippet, params)
        assertEquals(snippet, output)
    }
}
