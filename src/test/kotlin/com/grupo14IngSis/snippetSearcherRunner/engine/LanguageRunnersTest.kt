package com.grupo14IngSis.snippetSearcherRunner.engine

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LanguageRunnersTest {
    private val formattingPlugin = mockk<FormattingPlugin>()
    private val analyzerPlugin = mockk<AnalyzerPlugin>()
    private val printScript = PrintScriptLanguageRunner(formattingPlugin, analyzerPlugin)

    @Test
    fun `el registro encuentra el runner sin importar mayusculas`() {
        val registry = LanguageRunnerRegistry(listOf(printScript))

        assertSame(printScript, registry.getRunner("PrintScript"))
        assertTrue(registry.isSupported("PRINTSCRIPT"))
        assertFalse(registry.isSupported("python"))
        assertNull(registry.getRunner("python"))
        assertEquals(setOf("printscript"), registry.getSupportedLanguages())
    }

    @Test
    fun `PrintScript declara su lenguaje y extension`() {
        assertEquals("printscript", printScript.language)
        assertEquals("ps", printScript.fileExtension)
    }

    @Test
    fun `execute devuelve COMPLETED sin salida`() {
        assertEquals(ExecutionOutput("COMPLETED", emptyList()), printScript.execute("println(1);", emptyList(), emptyMap()))
    }

    @Test
    fun `format devuelve el codigo formateado, o el original si el plugin no devuelve texto`() {
        val rules = mapOf<String, Any>("indent-inside-if" to true)
        every { formattingPlugin.run("let a=1;", mapOf("rules" to rules)) } returns "let a = 1;"
        assertEquals("let a = 1;", printScript.format("let a=1;", rules))

        every { formattingPlugin.run("x", any()) } returns 42
        assertEquals("x", printScript.format("x", emptyMap()))
    }

    @Test
    fun `lint marca como no compliant si el resultado menciona un error`() {
        every { analyzerPlugin.run("mal", any()) } returns "Error: identifier_format en 1:5"
        val bad = printScript.lint("mal", emptyMap())
        assertFalse(bad.isCompliant)
        assertEquals(listOf("Error: identifier_format en 1:5"), bad.messages)

        every { analyzerPlugin.run("bien", any()) } returns "sin problemas"
        assertTrue(printScript.lint("bien", emptyMap()).isCompliant)

        every { analyzerPlugin.run("raro", any()) } returns Unit
        assertEquals(LintOutput(true, listOf("")), printScript.lint("raro", emptyMap()))
    }
}
