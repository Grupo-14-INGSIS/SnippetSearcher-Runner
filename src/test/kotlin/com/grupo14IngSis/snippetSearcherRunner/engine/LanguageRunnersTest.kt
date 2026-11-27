package com.grupo14IngSis.snippetSearcherRunner.engine

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintOutcome
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintReport
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LanguageRunnersTest {
    private val validationPlugin = mockk<ValidationPlugin>()
    private val formattingPlugin = mockk<FormattingPlugin>()
    private val analyzerPlugin = mockk<AnalyzerPlugin>()
    private val printScript = PrintScriptLanguageRunner(validationPlugin, formattingPlugin, analyzerPlugin)
    private val registry = LanguageRunnerRegistry(listOf(printScript))

    @Test
    fun `el registro encuentra el runner sin importar mayusculas`() {
        assertSame(printScript, registry.getRunner("PrintScript"))
        assertTrue(registry.isSupported("PRINTSCRIPT"))
        assertFalse(registry.isSupported("python"))
        assertNull(registry.getRunner("python"))
        assertEquals(setOf("printscript"), registry.getSupportedLanguages())
    }

    @Test
    fun `require falla con un lenguaje desconocido`() {
        assertSame(printScript, registry.require("printscript"))
        val e = assertThrows<IllegalArgumentException> { registry.require("java") }
        assertEquals("Unsupported language: java", e.message)
    }

    @Test
    fun `PrintScript declara su lenguaje y extension`() {
        assertEquals("printscript", printScript.language)
        assertEquals("ps", printScript.fileExtension)
    }

    @Test
    fun `validate devuelve null si es valido y el mensaje si no`() {
        every { validationPlugin.run("println(1);", mapOf("version" to "1.1")) } returns "OK"
        assertNull(printScript.validate("println(1);", "1.1"))

        every { validationPlugin.run("println(", mapOf("version" to "1.1")) } returns "Syntax error at 1:9"
        assertEquals("Syntax error at 1:9", printScript.validate("println(", "1.1"))
    }

    @Test
    fun `format y lint pasan las reglas y la version a los plugins`() {
        val rules = mapOf<String, Any>("indent-inside-if" to true)
        every { formattingPlugin.run("let a=1;", rules + ("version" to "1.0")) } returns "let a = 1;"
        assertEquals("let a = 1;", printScript.format("let a=1;", rules, "1.0"))

        val report = LintReport(LintOutcome.COMPLIANT, "SUCCESS")
        every { analyzerPlugin.lint("let a = 1;", rules + ("version" to "1.1")) } returns report
        assertSame(report, printScript.lint("let a = 1;", rules, "1.1"))
    }
}
