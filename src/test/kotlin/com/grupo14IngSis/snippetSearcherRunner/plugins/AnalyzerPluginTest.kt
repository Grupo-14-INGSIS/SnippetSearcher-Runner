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

    // ===== lint(): lo que usa el linteo asíncrono =====

    private val camelCase = mapOf<String, Any>("identifier_format" to true, "version" to "1.1")

    @Test
    fun `lint reports compliant code`() {
        val report = analyzerPlugin.lint("let myVar: number = 5;\nprintln(myVar);", camelCase)
        assertEquals(LintOutcome.COMPLIANT, report.outcome)
    }

    @Test
    fun `lint reports rule violations as not compliant`() {
        val report = analyzerPlugin.lint("let my_var: number = 5;\nprintln(my_var);", camelCase)
        assertEquals(LintOutcome.NOT_COMPLIANT, report.outcome)
    }

    @Test
    fun `lint does not confuse the snippet text with an analyzer error`() {
        // Antes se buscaba "Error:" en lo que devolvía run(), que para código válido es el propio código
        val report = analyzerPlugin.lint("println(\"Error: not really\");", camelCase)
        assertEquals(LintOutcome.COMPLIANT, report.outcome)
    }

    @Test
    fun `lint reports code that cannot be parsed as invalid`() {
        val report = analyzerPlugin.lint("let = ;", camelCase)
        assertEquals(LintOutcome.INVALID, report.outcome)
    }
}
