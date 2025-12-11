package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertContains

class FormattingPluginTest {
    private lateinit var runner: Runner
    private lateinit var formattingPlugin: FormattingPlugin
    private lateinit var tempConfigFile: File

    @BeforeEach
    fun setUp() {
        formattingPlugin = FormattingPlugin()
        tempConfigFile = createTempFile("config", ".yml")
        tempConfigFile.writeText("some config")
    }

    @Test
    fun `run with null snippet should return empty string`() {
        val result = formattingPlugin.run(null, emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run with blank snippet should return empty string`() {
        val result = formattingPlugin.run("   ", emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `run without configFile should throw IllegalArgumentException`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                formattingPlugin.run("some snippet", emptyMap())
            }
        assertEquals("Configuration file path 'configFile' is required for formatting.", exception.message)
    }

    @Test
    fun `run with non-existent configFile should throw IllegalArgumentException`() {
        val nonExistentFilePath = "non/existent/file.config"
        val params = mapOf("configFile" to nonExistentFilePath)
        val exception =
            assertThrows<IllegalArgumentException> {
                formattingPlugin.run("some snippet", params)
            }
        assertEquals("Configuration file does not exist at path: $nonExistentFilePath", exception.message)
    }

    @Test
    fun `run with valid snippet and empty config should return same snippet`() {
        val snippet = "let a:number = 14;"
        tempConfigFile.writeText(
            "",
        )
        val params = mapOf("configFile" to tempConfigFile.absolutePath)
        val output = formattingPlugin.run(snippet, params) as String
        val expected = "let a:number = 14;"

        assertContains(output, expected)
    }

    @Test
    fun `run with valid snippet and config should return formatted snippet`() {
        val snippet = "let a:number = 14;"
        tempConfigFile.writeText(
            "enforce-spacing-after-colon-in-declaration: true\n" +
                "enforce-no-spacing-around-equals: true",
        )
        val params = mapOf("configFile" to tempConfigFile.absolutePath)
        val output = formattingPlugin.run(snippet, params) as String
        val expected = "let a: number=14;"

        assertContains(output, expected)
    }
}
