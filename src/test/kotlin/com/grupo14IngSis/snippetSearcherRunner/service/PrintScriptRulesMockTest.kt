package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptRulesMockTest {

    @Test
    fun `lintingRules returns LanguageRuleEntry with expected values`() {
        val entry: LanguageRuleEntry = PrintScriptRulesMock.lintingRules()

        assertEquals("printscript", entry.language)
        assertEquals(true, entry.rules["rule1"])
        assertEquals(false, entry.rules["rule2"])
        assertEquals(3, entry.rules["rule3"])
    }

    @Test
    fun `formattingRules returns LanguageRuleEntry with expected values`() {
        val entry: LanguageRuleEntry = PrintScriptRulesMock.formattingRules()

        assertEquals("printscript", entry.language)
        assertEquals(true, entry.rules["rule1"])
        assertEquals(false, entry.rules["rule2"])
        assertEquals(3, entry.rules["rule3"])
    }

    @Test
    fun `lintingRules and formattingRules produce equal results`() {
        val linting = PrintScriptRulesMock.lintingRules()
        val formatting = PrintScriptRulesMock.formattingRules()

        assertEquals(linting, formatting)
        assertTrue(linting.rules == formatting.rules)
    }
}