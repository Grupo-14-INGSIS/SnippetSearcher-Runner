package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class ValidationPluginTest {
    private lateinit var runner: Runner
    private lateinit var validationPlugin: ValidationPlugin

    @BeforeEach
    fun setUp() {
        validationPlugin = ValidationPlugin()
    }

    @Test
    fun `run with null snippet should return no errors found`() {
        val result = validationPlugin.run(null, emptyMap())
        assertEquals("No errors found.", result)
    }

    @Test
    fun `run with blank snippet should return no errors found`() {
        val result = validationPlugin.run("  ", emptyMap())
        assertEquals("No errors found.", result)
    }

    @Test
    fun `run with valid snippet should validate snippet`() {
        val snippet = "println(\"hello\");"
        val output = validationPlugin.run(snippet, emptyMap()) as String
        val expected = "File is syntactically and semantically valid"

        assertContains(output, expected)
    }

    @Test
    fun `run with validation output should return the output`() {
        val snippet = "println(\"buggy code);"
        val expected = "ERROR during validation"
        val result = validationPlugin.run(snippet, emptyMap()) as String
        assertContains(result, expected)
    }
}
