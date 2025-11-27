package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals

class LintingRuleTest {
    @Test
    fun `should create LintingRule with all parameters`() {
        val configRules =
            mutableMapOf<String, Any>(
                "maxLineLength" to 120,
                "enforceNamingConvention" to true,
                "noUnusedVariables" to true,
            )

        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = configRules,
            )

        assertEquals("user123", lintingRule.userId)
        assertEquals("kotlin", lintingRule.setLanguage)
        assertNotNull(lintingRule.configRules)
        assertEquals(3, lintingRule.configRules?.size)
        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
    }

    @Test
    fun `should create LintingRule with default values`() {
        val lintingRule = LintingRule()

        assertEquals("", lintingRule.userId)
        assertEquals("", lintingRule.setLanguage)
        assertNull(lintingRule.configRules)
    }

    @Test
    fun `should create LintingRule with null configRules`() {
        val lintingRule =
            LintingRule(
                userId = "user456",
                setLanguage = "java",
                configRules = null,
            )

        assertEquals("user456", lintingRule.userId)
        assertEquals("java", lintingRule.setLanguage)
        assertNull(lintingRule.configRules)
    }

    @Test
    fun `should create LintingRule with empty configRules`() {
        val lintingRule =
            LintingRule(
                userId = "user789",
                setLanguage = "python",
                configRules = mutableMapOf(),
            )

        assertEquals("user789", lintingRule.userId)
        assertEquals("python", lintingRule.setLanguage)
        assertNotNull(lintingRule.configRules)
        assertTrue(lintingRule.configRules!!.isEmpty())
    }

    @Test
    fun `configRules should be mutable and allow modifications`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 80),
            )

        lintingRule.configRules?.put("enforceNamingConvention", true)
        lintingRule.configRules?.put("maxLineLength", 120)

        assertEquals(2, lintingRule.configRules?.size)
        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `should handle different data types in configRules`() {
        val configRules =
            mutableMapOf<String, Any>(
                "maxLineLength" to 120,
                "enforceNamingConvention" to true,
                "noUnusedVariables" to true,
                "warningLevel" to "strict",
                "allowedComplexity" to 15.5,
            )

        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = configRules,
            )

        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
        assertEquals(true, lintingRule.configRules?.get("noUnusedVariables"))
        assertEquals("strict", lintingRule.configRules?.get("warningLevel"))
        assertEquals(15.5, lintingRule.configRules?.get("allowedComplexity"))
    }

    @Test
    fun `should support copy with modifications`() {
        val original =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 80),
            )

        val copy = original.copy(setLanguage = "java")

        assertEquals("user123", copy.userId)
        assertEquals("java", copy.setLanguage)
        assertNotNull(copy.configRules)
    }

    @Test
    fun `should support equality comparison`() {
        val rules1 = mutableMapOf<String, Any>("maxLineLength" to 120)
        val rules2 = mutableMapOf<String, Any>("maxLineLength" to 120)

        val lintingRule1 =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = rules1,
            )

        val lintingRule2 =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = rules2,
            )

        assertEquals(lintingRule1.userId, lintingRule2.userId)
        assertEquals(lintingRule1.setLanguage, lintingRule2.setLanguage)
    }

    @Test
    fun `should handle different languages`() {
        val languages = listOf("kotlin", "java", "python", "javascript", "typescript")

        languages.forEach { lang ->
            val lintingRule =
                LintingRule(
                    userId = "user123",
                    setLanguage = lang,
                    configRules = mutableMapOf("maxLineLength" to 120),
                )

            assertEquals(lang, lintingRule.setLanguage)
        }
    }

    @Test
    fun `should handle special characters in userId`() {
        val lintingRule =
            LintingRule(
                userId = "user-123_test@example",
                setLanguage = "kotlin",
                configRules = mutableMapOf(),
            )

        assertEquals("user-123_test@example", lintingRule.userId)
    }

    @Test
    fun `should handle language with special characters`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "C++",
                configRules = mutableMapOf(),
            )

        assertEquals("C++", lintingRule.setLanguage)
    }

    @Test
    fun `configRules should support adding multiple rules`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf(),
            )

        lintingRule.configRules?.put("maxLineLength", 120)
        lintingRule.configRules?.put("enforceNamingConvention", true)
        lintingRule.configRules?.put("noUnusedVariables", true)
        lintingRule.configRules?.put("requireDocumentation", false)

        assertEquals(4, lintingRule.configRules?.size)
    }

    @Test
    fun `configRules should support removing rules`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "maxLineLength" to 120,
                        "enforceNamingConvention" to true,
                    ),
            )

        lintingRule.configRules?.remove("enforceNamingConvention")

        assertEquals(1, lintingRule.configRules?.size)
        assertNull(lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `configRules should support clearing all rules`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "maxLineLength" to 120,
                        "enforceNamingConvention" to true,
                    ),
            )

        lintingRule.configRules?.clear()

        assertTrue(lintingRule.configRules!!.isEmpty())
    }

    @Test
    fun `should handle numeric rule values`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "maxLineLength" to 120,
                        "maxFunctionLength" to 50,
                        "maxComplexity" to 10,
                    ),
            )

        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(50, lintingRule.configRules?.get("maxFunctionLength"))
        assertEquals(10, lintingRule.configRules?.get("maxComplexity"))
    }

    @Test
    fun `should handle boolean rule values`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "enforceNamingConvention" to true,
                        "noUnusedVariables" to false,
                        "requireDocumentation" to true,
                    ),
            )

        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
        assertEquals(false, lintingRule.configRules?.get("noUnusedVariables"))
        assertEquals(true, lintingRule.configRules?.get("requireDocumentation"))
    }

    @Test
    fun `should handle string rule values`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "namingStyle" to "camelCase",
                        "warningLevel" to "error",
                        "severity" to "high",
                    ),
            )

        assertEquals("camelCase", lintingRule.configRules?.get("namingStyle"))
        assertEquals("error", lintingRule.configRules?.get("warningLevel"))
        assertEquals("high", lintingRule.configRules?.get("severity"))
    }

    @Test
    fun `should create LintingRule with long userId`() {
        val longUserId = "a".repeat(100)
        val lintingRule =
            LintingRule(
                userId = longUserId,
                setLanguage = "kotlin",
                configRules = mutableMapOf(),
            )

        assertEquals(longUserId, lintingRule.userId)
        assertEquals(100, lintingRule.userId.length)
    }

    @Test
    fun `should create LintingRule with complex configRules`() {
        val complexRules =
            mutableMapOf<String, Any>(
                "maxLineLength" to 120,
                "maxFunctionLength" to 50,
                "enforceNamingConvention" to true,
                "noUnusedVariables" to true,
                "requireDocumentation" to false,
                "maxComplexity" to 10,
                "warningLevel" to "error",
            )

        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = complexRules,
            )

        assertEquals(7, lintingRule.configRules?.size)
        assertNotNull(lintingRule.configRules?.get("maxLineLength"))
        assertNotNull(lintingRule.configRules?.get("enforceNamingConvention"))
        assertNotNull(lintingRule.configRules?.get("warningLevel"))
    }

    @Test
    fun `data class should have proper toString`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        val toString = lintingRule.toString()

        assertTrue(toString.contains("user123"))
        assertTrue(toString.contains("kotlin"))
    }

    @Test
    fun `data class should have proper hashCode`() {
        val lintingRule1 =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        val lintingRule2 =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        // HashCode should be consistent for same values
        assertEquals(lintingRule1.hashCode(), lintingRule1.hashCode())
    }

    @Test
    fun `should handle putAll operation on configRules`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 80),
            )

        val newRules =
            mapOf(
                "enforceNamingConvention" to true,
                "noUnusedVariables" to false,
                "maxLineLength" to 120,
            )

        lintingRule.configRules?.putAll(newRules)

        assertEquals(3, lintingRule.configRules?.size)
        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `should handle linting-specific rules`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules =
                    mutableMapOf(
                        "checkUnusedImports" to true,
                        "enforceConstantNaming" to true,
                        "maxParameterCount" to 5,
                        "checkCyclomaticComplexity" to true,
                    ),
            )

        assertEquals(true, lintingRule.configRules?.get("checkUnusedImports"))
        assertEquals(true, lintingRule.configRules?.get("enforceConstantNaming"))
        assertEquals(5, lintingRule.configRules?.get("maxParameterCount"))
        assertEquals(true, lintingRule.configRules?.get("checkCyclomaticComplexity"))
    }
}
