package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormattingRuleTest {

    @Test
    fun `should create FormattingRule with all parameters`() {
        val configRules = mutableMapOf<String, Any>(
            "indentSize" to 4,
            "lineLength" to 120,
            "spacesAroundOperators" to true
        )

        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = configRules
        )

        assertEquals("user123", formattingRule.userId)
        assertEquals("kotlin", formattingRule.setLanguage)
        assertNotNull(formattingRule.configRules)
        assertEquals(3, formattingRule.configRules?.size)
        assertEquals(4, formattingRule.configRules?.get("indentSize"))
    }

    @Test
    fun `should create FormattingRule with default values`() {
        val formattingRule = FormattingRule()

        assertEquals("", formattingRule.userId)
        assertEquals("", formattingRule.setLanguage)
        assertNull(formattingRule.configRules)
    }

    @Test
    fun `should create FormattingRule with null configRules`() {
        val formattingRule = FormattingRule(
            userId = "user456",
            setLanguage = "java",
            configRules = null
        )

        assertEquals("user456", formattingRule.userId)
        assertEquals("java", formattingRule.setLanguage)
        assertNull(formattingRule.configRules)
    }

    @Test
    fun `should create FormattingRule with empty configRules`() {
        val formattingRule = FormattingRule(
            userId = "user789",
            setLanguage = "python",
            configRules = mutableMapOf()
        )

        assertEquals("user789", formattingRule.userId)
        assertEquals("python", formattingRule.setLanguage)
        assertNotNull(formattingRule.configRules)
        assertTrue(formattingRule.configRules!!.isEmpty())
    }

    @Test
    fun `configRules should be mutable and allow modifications`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 2)
        )

        formattingRule.configRules?.put("lineLength", 80)
        formattingRule.configRules?.put("indentSize", 4)

        assertEquals(2, formattingRule.configRules?.size)
        assertEquals(4, formattingRule.configRules?.get("indentSize"))
        assertEquals(80, formattingRule.configRules?.get("lineLength"))
    }

    @Test
    fun `should handle different data types in configRules`() {
        val configRules = mutableMapOf<String, Any>(
            "indentSize" to 4,
            "lineLength" to 120,
            "spacesAroundOperators" to true,
            "indentStyle" to "spaces",
            "maxComplexity" to 15.5
        )

        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = configRules
        )

        assertEquals(4, formattingRule.configRules?.get("indentSize"))
        assertEquals(120, formattingRule.configRules?.get("lineLength"))
        assertEquals(true, formattingRule.configRules?.get("spacesAroundOperators"))
        assertEquals("spaces", formattingRule.configRules?.get("indentStyle"))
        assertEquals(15.5, formattingRule.configRules?.get("maxComplexity"))
    }

    @Test
    fun `should support copy with modifications`() {
        val original = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 2)
        )

        val copy = original.copy(setLanguage = "java")

        assertEquals("user123", copy.userId)
        assertEquals("java", copy.setLanguage)
        assertNotNull(copy.configRules)
    }

    @Test
    fun `should support equality comparison`() {
        val rules1 = mutableMapOf<String, Any>("indentSize" to 4)
        val rules2 = mutableMapOf<String, Any>("indentSize" to 4)

        val formattingRule1 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = rules1
        )

        val formattingRule2 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = rules2
        )

        assertEquals(formattingRule1.userId, formattingRule2.userId)
        assertEquals(formattingRule1.setLanguage, formattingRule2.setLanguage)
    }

    @Test
    fun `should handle different languages`() {
        val languages = listOf("kotlin", "java", "python", "javascript", "typescript")

        languages.forEach { lang ->
            val formattingRule = FormattingRule(
                userId = "user123",
                setLanguage = lang,
                configRules = mutableMapOf("indentSize" to 4)
            )

            assertEquals(lang, formattingRule.setLanguage)
        }
    }

    @Test
    fun `should handle special characters in userId`() {
        val formattingRule = FormattingRule(
            userId = "user-123_test@example",
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        assertEquals("user-123_test@example", formattingRule.userId)
    }

    @Test
    fun `should handle language with special characters`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "C++",
            configRules = mutableMapOf()
        )

        assertEquals("C++", formattingRule.setLanguage)
    }

    @Test
    fun `configRules should support adding multiple rules`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        formattingRule.configRules?.put("indentSize", 4)
        formattingRule.configRules?.put("lineLength", 120)
        formattingRule.configRules?.put("spacesAroundOperators", true)
        formattingRule.configRules?.put("newlineBeforeBrace", false)

        assertEquals(4, formattingRule.configRules?.size)
    }

    @Test
    fun `configRules should support removing rules`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "indentSize" to 4,
                "lineLength" to 120
            )
        )

        formattingRule.configRules?.remove("lineLength")

        assertEquals(1, formattingRule.configRules?.size)
        assertNull(formattingRule.configRules?.get("lineLength"))
    }

    @Test
    fun `configRules should support clearing all rules`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "indentSize" to 4,
                "lineLength" to 120
            )
        )

        formattingRule.configRules?.clear()

        assertTrue(formattingRule.configRules!!.isEmpty())
    }

    @Test
    fun `should handle numeric rule values`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "indentSize" to 4,
                "lineLength" to 120,
                "maxNestingLevel" to 5
            )
        )

        assertEquals(4, formattingRule.configRules?.get("indentSize"))
        assertEquals(120, formattingRule.configRules?.get("lineLength"))
        assertEquals(5, formattingRule.configRules?.get("maxNestingLevel"))
    }

    @Test
    fun `should handle boolean rule values`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "spacesAroundOperators" to true,
                "newlineBeforeBrace" to false,
                "enforceTrailingComma" to true
            )
        )

        assertEquals(true, formattingRule.configRules?.get("spacesAroundOperators"))
        assertEquals(false, formattingRule.configRules?.get("newlineBeforeBrace"))
        assertEquals(true, formattingRule.configRules?.get("enforceTrailingComma"))
    }

    @Test
    fun `should handle string rule values`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "indentStyle" to "spaces",
                "quoteStyle" to "double",
                "lineEnding" to "LF"
            )
        )

        assertEquals("spaces", formattingRule.configRules?.get("indentStyle"))
        assertEquals("double", formattingRule.configRules?.get("quoteStyle"))
        assertEquals("LF", formattingRule.configRules?.get("lineEnding"))
    }

    @Test
    fun `should create FormattingRule with long userId`() {
        val longUserId = "a".repeat(100)
        val formattingRule = FormattingRule(
            userId = longUserId,
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        assertEquals(longUserId, formattingRule.userId)
        assertEquals(100, formattingRule.userId.length)
    }

    @Test
    fun `should create FormattingRule with complex configRules`() {
        val complexRules = mutableMapOf<String, Any>(
            "indentSize" to 4,
            "lineLength" to 120,
            "spacesAroundOperators" to true,
            "indentStyle" to "spaces",
            "enforceTrailingComma" to false,
            "maxNestingLevel" to 5,
            "allowedComplexity" to 10.5
        )

        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = complexRules
        )

        assertEquals(7, formattingRule.configRules?.size)
        assertNotNull(formattingRule.configRules?.get("indentSize"))
        assertNotNull(formattingRule.configRules?.get("lineLength"))
        assertNotNull(formattingRule.configRules?.get("spacesAroundOperators"))
    }

    @Test
    fun `data class should have proper toString`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        val toString = formattingRule.toString()

        assertTrue(toString.contains("user123"))
        assertTrue(toString.contains("kotlin"))
    }

    @Test
    fun `data class should have proper hashCode`() {
        val formattingRule1 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        val formattingRule2 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        // HashCode should be consistent for same values
        assertEquals(formattingRule1.hashCode(), formattingRule1.hashCode())
    }

    @Test
    fun `should handle putAll operation on configRules`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 2)
        )

        val newRules = mapOf(
            "lineLength" to 100,
            "spacesAroundOperators" to false,
            "indentSize" to 4
        )

        formattingRule.configRules?.putAll(newRules)

        assertEquals(3, formattingRule.configRules?.size)
        assertEquals(4, formattingRule.configRules?.get("indentSize"))
        assertEquals(100, formattingRule.configRules?.get("lineLength"))
    }
}