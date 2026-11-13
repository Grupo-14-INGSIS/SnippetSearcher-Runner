package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PrintScriptRulesProviderTest {
    private val rulesProvider = PrintScriptRulesProvider()

    @Test
    fun `getLintingRules returns LanguageRuleEntry with expected values`() {
        val entry: LanguageRuleEntry = rulesProvider.getLintingRules(null)

        assertEquals("printscript", entry.language)
        assertEquals(true, entry.rules["identifier_format"])
        assertEquals(true, entry.rules["mandatory-variable-or-literal-in-println"])
        assertEquals(true, entry.rules["mandatory-variable-or-literal-in-readInput"])
    }

    @Test
    fun `getFormattingRules returns LanguageRuleEntry with expected values`() {
        val entry: LanguageRuleEntry = rulesProvider.getFormattingRules(null)

        assertEquals("printscript", entry.language)
        assertEquals(true, entry.rules["enforce-spacing-around-equals"])
        assertEquals(false, entry.rules["enforce-no-spacing-around-equals"])
        assertEquals(false, entry.rules["enforce-spacing-before-colon-in-declaration"])
        assertEquals(true, entry.rules["enforce-spacing-after-colon-in-declaration"])
        // Números, no booleanos: la librería las lee como cantidad de líneas en blanco y de espacios de sangría
        assertEquals(0, entry.rules["line-breaks-after-println"])
        assertEquals(0, entry.rules["line-breaks-before-println"])
        assertEquals(2, entry.rules["indent-inside-if"])
        assertEquals(true, entry.rules["if-brace-below-line"])
        assertEquals(true, entry.rules["mandatory-single-space-separation"])
        assertEquals(true, entry.rules["mandatory-space-surrounding-operations"])
        assertEquals(true, entry.rules["mandatory-line-break-after-statement"])
    }
}
