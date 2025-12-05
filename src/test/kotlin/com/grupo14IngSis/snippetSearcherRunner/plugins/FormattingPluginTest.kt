package com.grupo14IngSis.snippetSearcherRunner.plugins

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class FormattingPluginTest {
    private lateinit var runner: Runner
    private lateinit var formattingPlugin: FormattingPlugin
    private lateinit var tempConfigFile: File

    @BeforeEach
    fun setUp() {
        runner = mockk(relaxed = true)
        formattingPlugin = FormattingPlugin(runner)
        tempConfigFile = createTempFile("config", ".ps")
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
    fun `run with valid snippet and config should call formatterCommand`() {
        val snippet = "println(\"hello\");"
        val params = mapOf("configFile" to tempConfigFile.absolutePath)

        formattingPlugin.run(snippet, params)

        verify { runner.formatterCommand(any()) }
    }

    @Test
    fun `run with version should call formatterCommand with version`() {
        val snippet = "println(\"hello\");"
        val version = "1.1"
        val params = mapOf("configFile" to tempConfigFile.absolutePath, "version" to version)

        formattingPlugin.run(snippet, params)

        verify { runner.formatterCommand(match { it.contains(version) }) }
    }

    @Test
    fun `formatterCommand should modify the snippet and return it`() {
        val snippet = "let a: number = 1"
        val formattedSnippet = "let a: number = 1;"
        val params = mapOf("configFile" to tempConfigFile.absolutePath)

        every { runner.formatterCommand(any()) } answers {
            val tempFile = File(firstArg<List<String>>()[0])
            tempFile.writeText(formattedSnippet)
        }

        val result = formattingPlugin.run(snippet, params)

        assertEquals(formattedSnippet, result)
    }
}
