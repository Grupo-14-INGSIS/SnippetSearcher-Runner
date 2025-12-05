package com.grupo14IngSis.snippetSearcherRunner.plugins

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.Runner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ValidationPluginTest {

    private lateinit var runner: Runner
    private lateinit var validationPlugin: ValidationPlugin

    @BeforeEach
    fun setUp() {
        runner = mockk(relaxed = true)
        validationPlugin = ValidationPlugin(runner)
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
    fun `run with valid snippet should call validationCommand`() {
        val snippet = "println(\"hello\");"
        validationPlugin.run(snippet, emptyMap())

        verify { runner.validationCommand(any()) }
    }
    
    @Test
    fun `run with version should call validationCommand with version`() {
        val snippet = "println(\"hello\");"
        val version = "1.1"
        val params = mapOf("version" to version)

        validationPlugin.run(snippet, params)

        verify { runner.validationCommand(match { it.contains(version) }) }
    }

    @Test
    fun `run with no validation output should return no errors found`() {
        val snippet = "println(\"perfect code\");"
    
        val result = validationPlugin.run(snippet, emptyMap())
    
        assertEquals("No errors found.", result)
    }
    
    @Test
    fun `run with validation output should return the output`() {
        val snippet = "println(\"buggy code\");"
        val validationOutput = "Error on line 1"
    
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))
    
        every { runner.validationCommand(any()) } answers {
            print(validationOutput)
        }
    
        val result = validationPlugin.run(snippet, emptyMap())
    
        System.setOut(originalOut)
        assertEquals(validationOutput, result)
    }
}