package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LintingRuleTest {

    @Test
    fun `should create LintingRule with all parameters`() {
        val configRules = mutableMapOf<String, Any>(
            "maxLineLength" to 120,
            "enforceNamingConvention" to true,
            "noUnusedVariables" to true
        )

        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = configRules
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
        val lintingRule = LintingRule(
            userId = "user456",
            setLanguage = "java",
            configRules = null
        )

        assertEquals("user456", lintingRule.userId)
        assertEquals("java", lintingRule.setLanguage)
        assertNull(lintingRule.configRules)
    }

    @Test
    fun `should create LintingRule with empty configRules`() {
        val lintingRule = LintingRule(
            userId = "user789",
            setLanguage = "python",
            configRules = mutableMapOf()
        )

        assertEquals("user789", lintingRule.userId)
        assertEquals("python", lintingRule.setLanguage)
        assertNotNull(lintingRule.configRules)
        assertTrue(lintingRule.configRules!!.isEmpty())
    }

    @Test
    fun `configRules should be mutable and allow modifications`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 80)
        )

        lintingRule.configRules?.put("enforceNamingConvention", true)
        lintingRule.configRules?.put("maxLineLength", 120)

        assertEquals(2, lintingRule.configRules?.size)
        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `should handle different data types in configRules`() {
        val configRules = mutableMapOf<String, Any>(
            "maxLineLength" to 120,
            "enforceNamingConvention" to true,
            "noUnusedVariables" to true,
            "warningLevel" to "strict",
            "allowedComplexity" to 15.5
        )

        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = configRules
        )

        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
        assertEquals(true, lintingRule.configRules?.get("noUnusedVariables"))
        assertEquals("strict", lintingRule.configRules?.get("warningLevel"))
        assertEquals(15.5, lintingRule.configRules?.get("allowedComplexity"))
    }

    @Test
    fun `should support copy with modifications`() {
        val original = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 80)
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

        val lintingRule1 = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = rules1
        )

        val lintingRule2 = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = rules2
        )

        assertEquals(lintingRule1.userId, lintingRule2.userId)
        assertEquals(lintingRule1.setLanguage, lintingRule2.setLanguage)
    }

    @Test
    fun `should handle different languages`() {
        val languages = listOf("kotlin", "java", "python", "javascript", "typescript")

        languages.forEach { lang ->
            val lintingRule = LintingRule(
                userId = "user123",
                setLanguage = lang,
                configRules = mutableMapOf("maxLineLength" to 120)
            )

            assertEquals(lang, lintingRule.setLanguage)
        }
    }

    @Test
    fun `should handle special characters in userId`() {
        val lintingRule = LintingRule(
            userId = "user-123_test@example",
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        assertEquals("user-123_test@example", lintingRule.userId)
    }

    @Test
    fun `should handle language with special characters`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "C++",
            configRules = mutableMapOf()
        )

        assertEquals("C++", lintingRule.setLanguage)
    }

    @Test
    fun `configRules should support adding multiple rules`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        lintingRule.configRules?.put("maxLineLength", 120)
        lintingRule.configRules?.put("enforceNamingConvention", true)
        lintingRule.configRules?.put("noUnusedVariables", true)
        lintingRule.configRules?.put("requireDocumentation", false)

        assertEquals(4, lintingRule.configRules?.size)
    }

    @Test
    fun `configRules should support removing rules`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "maxLineLength" to 120,
                "enforceNamingConvention" to true
            )
        )

        lintingRule.configRules?.remove("enforceNamingConvention")

        assertEquals(1, lintingRule.configRules?.size)
        assertNull(lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `configRules should support clearing all rules`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "maxLineLength" to 120,
                "enforceNamingConvention" to true
            )
        )

        lintingRule.configRules?.clear()

        assertTrue(lintingRule.configRules!!.isEmpty())
    }

    @Test
    fun `should handle numeric rule values`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "maxLineLength" to 120,
                "maxFunctionLength" to 50,
                "maxComplexity" to 10
            )
        )

        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(50, lintingRule.configRules?.get("maxFunctionLength"))
        assertEquals(10, lintingRule.configRules?.get("maxComplexity"))
    }

    @Test
    fun `should handle boolean rule values`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "enforceNamingConvention" to true,
                "noUnusedVariables" to false,
                "requireDocumentation" to true
            )
        )

        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
        assertEquals(false, lintingRule.configRules?.get("noUnusedVariables"))
        assertEquals(true, lintingRule.configRules?.get("requireDocumentation"))
    }

    @Test
    fun `should handle string rule values`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "namingStyle" to "camelCase",
                "warningLevel" to "error",
                "severity" to "high"
            )
        )

        assertEquals("camelCase", lintingRule.configRules?.get("namingStyle"))
        assertEquals("error", lintingRule.configRules?.get("warningLevel"))
        assertEquals("high", lintingRule.configRules?.get("severity"))
    }

    @Test
    fun `should create LintingRule with long userId`() {
        val longUserId = "a".repeat(100)
        val lintingRule = LintingRule(
            userId = longUserId,
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        assertEquals(longUserId, lintingRule.userId)
        assertEquals(100, lintingRule.userId.length)
    }

    @Test
    fun `should create LintingRule with complex configRules`() {
        val complexRules = mutableMapOf<String, Any>(
            "maxLineLength" to 120,
            "maxFunctionLength" to 50,
            "enforceNamingConvention" to true,
            "noUnusedVariables" to true,
            "requireDocumentation" to false,
            "maxComplexity" to 10,
            "warningLevel" to "error"
        )

        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = complexRules
        )

        assertEquals(7, lintingRule.configRules?.size)
        assertNotNull(lintingRule.configRules?.get("maxLineLength"))
        assertNotNull(lintingRule.configRules?.get("enforceNamingConvention"))
        assertNotNull(lintingRule.configRules?.get("warningLevel"))
    }

    @Test
    fun `data class should have proper toString`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        val toString = lintingRule.toString()

        assertTrue(toString.contains("user123"))
        assertTrue(toString.contains("kotlin"))
    }

    @Test
    fun `data class should have proper hashCode`() {
        val lintingRule1 = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        val lintingRule2 = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        // HashCode should be consistent for same values
        assertEquals(lintingRule1.hashCode(), lintingRule1.hashCode())
    }

    @Test
    fun `should handle putAll operation on configRules`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 80)
        )

        val newRules = mapOf(
            "enforceNamingConvention" to true,
            "noUnusedVariables" to false,
            "maxLineLength" to 120
        )

        lintingRule.configRules?.putAll(newRules)

        assertEquals(3, lintingRule.configRules?.size)
        assertEquals(120, lintingRule.configRules?.get("maxLineLength"))
        assertEquals(true, lintingRule.configRules?.get("enforceNamingConvention"))
    }

    @Test
    fun `should handle linting-specific rules`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf(
                "checkUnusedImports" to true,
                "enforceConstantNaming" to true,
                "maxParameterCount" to 5,
                "checkCyclomaticComplexity" to true
            )
        )

        assertEquals(true, lintingRule.configRules?.get("checkUnusedImports"))
        assertEquals(true, lintingRule.configRules?.get("enforceConstantNaming"))
        assertEquals(5, lintingRule.configRules?.get("maxParameterCount"))
        assertEquals(true, lintingRule.configRules?.get("checkCyclomaticComplexity"))
    }
}

class LintingRuleIdTest {

    @Test
    fun `should create LintingRuleId with all parameters`() {
        val id = LintingRuleId(
            userId = "user123",
            setLanguage = "kotlin"
        )

        assertEquals("user123", id.userId)
        assertEquals("kotlin", id.setLanguage)
    }

    @Test
    fun `should create LintingRuleId with default values`() {
        val id = LintingRuleId()

        assertEquals("", id.userId)
        assertEquals("", id.setLanguage)
    }

    @Test
    fun `should be equal when userId and setLanguage are the same`() {
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "kotlin")

        assertEquals(id1, id2)
    }

    @Test
    fun `should not be equal when userId is different`() {
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user456", "kotlin")

        assertNotEquals(id1, id2)
    }

    @Test
    fun `should not be equal when setLanguage is different`() {
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "java")

        assertNotEquals(id1, id2)
    }

    @Test
    fun `should have same hashCode for equal objects`() {
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "kotlin")

        assertEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun `should be serializable`() {
        val id = LintingRuleId("user123", "kotlin")

        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(id)
        objectOutputStream.close()

        val byteArray = byteArrayOutputStream.toByteArray()
        assertTrue(byteArray.isNotEmpty())
    }

    @Test
    fun `should deserialize correctly`() {
        val original = LintingRuleId("user123", "kotlin")

        // Serialize
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(original)
        objectOutputStream.close()

        // Deserialize
        val byteArray = byteArrayOutputStream.toByteArray()
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val deserialized = objectInputStream.readObject() as LintingRuleId

        assertEquals(original, deserialized)
        assertEquals(original.userId, deserialized.userId)
        assertEquals(original.setLanguage, deserialized.setLanguage)
    }

    @Test
    fun `should work as map key`() {
        val map = mutableMapOf<LintingRuleId, String>()
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "kotlin")

        map[id1] = "value1"
        map[id2] = "value2"

        assertEquals(1, map.size)
        assertEquals("value2", map[id1])
    }

    @Test
    fun `should work in sets`() {
        val set = mutableSetOf<LintingRuleId>()
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "kotlin")
        val id3 = LintingRuleId("user456", "java")

        set.add(id1)
        set.add(id2)
        set.add(id3)

        assertEquals(2, set.size)
    }
}

class LintingRuleAndIdIntegrationTest {

    @Test
    fun `LintingRule should use LintingRuleId correctly`() {
        val userId = "user123"
        val language = "kotlin"
        val configRules = mutableMapOf<String, Any>("maxLineLength" to 120)

        val lintingRule = LintingRule(
            userId = userId,
            setLanguage = language,
            configRules = configRules
        )

        val lintingRuleId = LintingRuleId(userId, language)

        assertEquals(lintingRule.userId, lintingRuleId.userId)
        assertEquals(lintingRule.setLanguage, lintingRuleId.setLanguage)
    }

    @Test
    fun `should create LintingRuleId from LintingRule`() {
        val lintingRule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        val id = LintingRuleId(lintingRule.userId, lintingRule.setLanguage)

        assertEquals("user123", id.userId)
        assertEquals("kotlin", id.setLanguage)
    }

    @Test
    fun `should use LintingRuleId as composite key in map`() {
        val map = mutableMapOf<LintingRuleId, LintingRule>()

        val id = LintingRuleId("user123", "kotlin")
        val rule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        map[id] = rule

        assertEquals(rule, map[id])
        assertEquals(1, map.size)
    }

    @Test
    fun `should handle multiple rules for same user with different languages`() {
        val map = mutableMapOf<LintingRuleId, LintingRule>()
        val userId = "user123"

        val kotlinRule = LintingRule(
            userId = userId,
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120)
        )

        val javaRule = LintingRule(
            userId = userId,
            setLanguage = "java",
            configRules = mutableMapOf("maxLineLength" to 100)
        )

        map[LintingRuleId(userId, "kotlin")] = kotlinRule
        map[LintingRuleId(userId, "java")] = javaRule

        assertEquals(2, map.size)
        assertEquals(120, map[LintingRuleId(userId, "kotlin")]?.configRules?.get("maxLineLength"))
        assertEquals(100, map[LintingRuleId(userId, "java")]?.configRules?.get("maxLineLength"))
    }

    @Test
    fun `should retrieve LintingRule using LintingRuleId`() {
        val storage = mutableMapOf<LintingRuleId, LintingRule>()

        val id = LintingRuleId("user123", "kotlin")
        val rule = LintingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("maxLineLength" to 120, "enforceNamingConvention" to true)
        )

        storage[id] = rule

        val retrieved = storage[id]
        assertNotNull(retrieved)
        assertEquals(rule.userId, retrieved?.userId)
        assertEquals(rule.setLanguage, retrieved?.setLanguage)
        assertEquals(2, retrieved?.configRules?.size)
    }

    @Test
    fun `should filter rules by userId using LintingRuleId`() {
        val storage = mutableMapOf<LintingRuleId, LintingRule>()

        storage[LintingRuleId("user123", "kotlin")] = LintingRule("user123", "kotlin")
        storage[LintingRuleId("user123", "java")] = LintingRule("user123", "java")
        storage[LintingRuleId("user456", "kotlin")] = LintingRule("user456", "kotlin")

        val user123Rules = storage.filter { it.key.userId == "user123" }

        assertEquals(2, user123Rules.size)
    }
}