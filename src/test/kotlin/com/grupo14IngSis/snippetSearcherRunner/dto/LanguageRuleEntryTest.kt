package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LanguageRuleEntryTest {
    @Test
    fun `create instance and access properties`() {
        val rules = mapOf("indent" to 4, "maxLineLength" to 120)
        val entry = LanguageRuleEntry(language = "Kotlin", rules = rules)

        assertEquals("Kotlin", entry.language)
        assertEquals(4, entry.rules["indent"])
        assertEquals(120, entry.rules["maxLineLength"])
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val rules = mapOf("rule1" to true)
        val entry1 = LanguageRuleEntry("Kotlin", rules)
        val entry2 = LanguageRuleEntry("Kotlin", rules)
        val entry3 = LanguageRuleEntry("Java", rules)

        assertEquals(entry1, entry2)
        assertEquals(entry1.hashCode(), entry2.hashCode())
        assertNotEquals(entry1, entry3)
    }

    @Test
    fun `copy creates new instance with modifications`() {
        val entry = LanguageRuleEntry("Kotlin", mapOf("rule1" to true))
        val copy = entry.copy(language = "Java")

        assertEquals("Java", copy.language)
        assertEquals(entry.rules, copy.rules)
        assertNotEquals(entry, copy)
    }

    @Test
    fun `destructuring works`() {
        val entry = LanguageRuleEntry("Kotlin", mapOf("rule1" to true))
        val (lang, rules) = entry

        assertEquals("Kotlin", lang)
        assertTrue(rules["rule1"] as Boolean)
    }

    @Test
    fun `toString contains class name and properties`() {
        val entry = LanguageRuleEntry("Kotlin", mapOf("rule1" to true))
        val str = entry.toString()

        assertTrue(str.contains("LanguageRuleEntry"))
        assertTrue(str.contains("Kotlin"))
        assertTrue(str.contains("rule1"))
    }
}
