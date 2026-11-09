package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RuleValueTest {
    @Test
    fun `guarda cada tipo con su nombre`() {
        assertEquals(RuleValue("true", RuleValue.BOOLEAN), RuleValue.of(true))
        assertEquals(RuleValue("2", RuleValue.NUMBER), RuleValue.of(2))
        assertEquals(RuleValue("camel case", RuleValue.STRING), RuleValue.of("camel case"))
    }

    @Test
    fun `devuelve cada valor con su tipo original`() {
        assertEquals(true, RuleValue.of(true).toAny())
        assertEquals(false, RuleValue.of(false).toAny())
        assertEquals(2, RuleValue.of(2).toAny())
        assertEquals(10_000_000_000L, RuleValue.of(10_000_000_000L).toAny())
        assertEquals(1.5, RuleValue.of(1.5).toAny())
        assertEquals("SOCOTROCO", RuleValue.of("SOCOTROCO").toAny())
    }

    @Test
    fun `un texto que parece booleano sigue siendo texto`() {
        assertEquals("true", RuleValue.of("true").toAny())
    }

    @Test
    fun `convierte mapas de ida y vuelta`() {
        val rules = mapOf("indent-inside-if" to true, "SOCOTROCO_AMOUNT" to 1, "SOCOTROCO_NAME" to "SOCOTROCO")
        assertEquals(rules, RuleValue.toMap(RuleValue.fromMap(rules)))
    }

    @Test
    fun `mapas nulos quedan vacios`() {
        assertTrue(RuleValue.fromMap(null).isEmpty())
        assertTrue(RuleValue.toMap(null).isEmpty())
    }

    @Test
    fun `FormattingRule arma sus filas al crearse y las actualiza con syncValues`() {
        val rule = FormattingRule("u1", "printscript", mutableMapOf("indent-inside-if" to true))
        assertEquals(mapOf("indent-inside-if" to RuleValue("true", RuleValue.BOOLEAN)), rule.values)

        rule.configRules!!["indent-inside-if"] = false
        rule.configRules!!["SOCOTROCO_AMOUNT"] = 3
        rule.syncValues()

        assertEquals(RuleValue("false", RuleValue.BOOLEAN), rule.values["indent-inside-if"])
        assertEquals(RuleValue("3", RuleValue.NUMBER), rule.values["SOCOTROCO_AMOUNT"])
    }

    @Test
    fun `FormattingRule rearma el mapa al leerse de la base`() {
        val rule = FormattingRule("u1", "printscript")
        rule.values["identifier_format"] = RuleValue("true", RuleValue.BOOLEAN)
        rule.loadConfigRules()
        assertEquals(mapOf<String, Any>("identifier_format" to true), rule.configRules)
    }

    @Test
    fun `LintingRule arma sus filas, las actualiza y rearma el mapa`() {
        val rule = LintingRule("u1", "printscript", mutableMapOf("identifier_format" to true))
        rule.configRules!!.remove("identifier_format")
        rule.configRules!!["mandatory-variable-or-literal-in-println"] = false
        rule.syncValues()
        assertEquals(setOf("mandatory-variable-or-literal-in-println"), rule.values.keys)

        rule.configRules = null
        rule.loadConfigRules()
        assertEquals(mapOf<String, Any>("mandatory-variable-or-literal-in-println" to false), rule.configRules)
    }
}
