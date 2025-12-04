package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

class LintingRuleIdTest {
    @Test
    fun `should create LintingRuleId with all parameters`() {
        val id =
            LintingRuleId(
                userId = "user123",
                setLanguage = "kotlin",
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
    fun `should have different hashCode for different objects`() {
        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user456", "java")

        assertNotEquals(id1.hashCode(), id2.hashCode())
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
    fun `should support copy with modifications`() {
        val original = LintingRuleId("user123", "kotlin")
        val modified = original.copy(setLanguage = "java")

        assertEquals("user123", modified.userId)
        assertEquals("java", modified.setLanguage)
        assertNotEquals(original, modified)
    }

    @Test
    fun `should support copy with userId modification`() {
        val original = LintingRuleId("user123", "kotlin")
        val modified = original.copy(userId = "user456")

        assertEquals("user456", modified.userId)
        assertEquals("kotlin", modified.setLanguage)
        assertNotEquals(original, modified)
    }

    @Test
    fun `should handle special characters in userId`() {
        val id = LintingRuleId("user-123_test@example", "kotlin")

        assertEquals("user-123_test@example", id.userId)
    }

    @Test
    fun `should handle special characters in setLanguage`() {
        val id = LintingRuleId("user123", "C++")

        assertEquals("C++", id.setLanguage)
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

    @Test
    fun `should have proper toString representation`() {
        val id = LintingRuleId("user123", "kotlin")
        val toString = id.toString()

        assertTrue(toString.contains("user123"))
        assertTrue(toString.contains("kotlin"))
    }

    @Test
    fun `should handle empty strings`() {
        val id = LintingRuleId("", "")

        assertEquals("", id.userId)
        assertEquals("", id.setLanguage)
    }

    @Test
    fun `should be usable with different languages`() {
        val languages = listOf("kotlin", "java", "python", "javascript", "typescript")
        val ids = languages.map { LintingRuleId("user123", it) }

        assertEquals(5, ids.size)
        assertEquals(5, ids.toSet().size)
    }

    @Test
    fun `should handle long userId`() {
        val longUserId = "a".repeat(100)
        val id = LintingRuleId(longUserId, "kotlin")

        assertEquals(longUserId, id.userId)
        assertEquals(100, id.userId.length)
    }

    @Test
    fun `should handle long setLanguage`() {
        val longLanguage = "l".repeat(50)
        val id = LintingRuleId("user123", longLanguage)

        assertEquals(longLanguage, id.setLanguage)
        assertEquals(50, id.setLanguage.length)
    }

    @Test
    fun `data class components should match fields`() {
        val id = LintingRuleId("user123", "kotlin")
        val (userId, setLanguage) = id

        assertEquals("user123", userId)
        assertEquals("kotlin", setLanguage)
    }

    @Test
    fun `should support destructuring in loops`() {
        val ids =
            listOf(
                LintingRuleId("user1", "kotlin"),
                LintingRuleId("user2", "java"),
                LintingRuleId("user3", "python"),
            )

        val userIds = mutableListOf<String>()
        val languages = mutableListOf<String>()

        for ((userId, language) in ids) {
            userIds.add(userId)
            languages.add(language)
        }

        assertEquals(listOf("user1", "user2", "user3"), userIds)
        assertEquals(listOf("kotlin", "java", "python"), languages)
    }

    @Test
    fun `should be comparable in maps with different key types`() {
        val stringMap = mutableMapOf<String, String>()
        val idMap = mutableMapOf<LintingRuleId, String>()

        val id = LintingRuleId("user123", "kotlin")
        stringMap["user123-kotlin"] = "value1"
        idMap[id] = "value2"

        assertEquals("value1", stringMap["user123-kotlin"])
        assertEquals("value2", idMap[id])
    }

    @Test
    fun `should maintain consistency across multiple operations`() {
        val id = LintingRuleId("user123", "kotlin")

        val copy1 = id.copy()
        val copy2 = id.copy()

        assertEquals(id, copy1)
        assertEquals(id, copy2)
        assertEquals(copy1, copy2)
        assertEquals(id.hashCode(), copy1.hashCode())
        assertEquals(id.hashCode(), copy2.hashCode())
    }

    @Test
    fun `should be usable in complex data structures`() {
        val nestedMap = mutableMapOf<LintingRuleId, MutableMap<String, Any>>()

        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user123", "java")

        nestedMap[id1] = mutableMapOf("maxLineLength" to 120)
        nestedMap[id2] = mutableMapOf("maxLineLength" to 100)

        assertEquals(2, nestedMap.size)
        assertEquals(120, nestedMap[id1]?.get("maxLineLength"))
        assertEquals(100, nestedMap[id2]?.get("maxLineLength"))
    }

    @Test
    fun `should handle unicode characters`() {
        val id = LintingRuleId("user-ñ-123", "日本語")

        assertEquals("user-ñ-123", id.userId)
        assertEquals("日本語", id.setLanguage)
    }

    @Test
    fun `should be immutable`() {
        val id = LintingRuleId("user123", "kotlin")

        // Intentar "modificar" debe crear una nueva instancia
        val modified = id.copy(userId = "user456")

        assertEquals("user123", id.userId)
        assertEquals("user456", modified.userId)
        assertNotSame(id, modified)
    }

    @Test
    fun `should serialize and deserialize with special characters`() {
        val original = LintingRuleId("user-123_@test", "C++")

        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(original)
        objectOutputStream.close()

        val byteArray = byteArrayOutputStream.toByteArray()
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val deserialized = objectInputStream.readObject() as LintingRuleId

        assertEquals(original, deserialized)
    }

    @Test
    fun `should handle case sensitivity correctly`() {
        val id1 = LintingRuleId("User123", "Kotlin")
        val id2 = LintingRuleId("user123", "kotlin")

        assertNotEquals(id1, id2)
        assertNotEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun `should work with list operations`() {
        val list = mutableListOf<LintingRuleId>()

        val id1 = LintingRuleId("user123", "kotlin")
        val id2 = LintingRuleId("user456", "java")
        val id3 = LintingRuleId("user123", "kotlin")

        list.add(id1)
        list.add(id2)
        list.add(id3)

        assertEquals(3, list.size)
        assertTrue(list.contains(id1))
        assertTrue(list.contains(id3))
        assertEquals(2, list.filter { it.userId == "user123" }.size)
    }

    @Test
    fun `should support grouping operations`() {
        val ids =
            listOf(
                LintingRuleId("user1", "kotlin"),
                LintingRuleId("user1", "java"),
                LintingRuleId("user2", "kotlin"),
                LintingRuleId("user2", "python"),
            )

        val groupedByUser = ids.groupBy { it.userId }
        val groupedByLanguage = ids.groupBy { it.setLanguage }

        assertEquals(2, groupedByUser.size)
        assertEquals(2, groupedByUser["user1"]?.size)
        assertEquals(3, groupedByLanguage.size)
        assertEquals(2, groupedByLanguage["kotlin"]?.size)
    }

    @Test
    fun `should maintain order in sorted collections`() {
        val ids =
            listOf(
                LintingRuleId("user3", "kotlin"),
                LintingRuleId("user1", "java"),
                LintingRuleId("user2", "python"),
            )

        val sortedByUser = ids.sortedBy { it.userId }

        assertEquals("user1", sortedByUser[0].userId)
        assertEquals("user2", sortedByUser[1].userId)
        assertEquals("user3", sortedByUser[2].userId)
    }
}

class LintingRuleAndIdIntegrationTest {
    @Test
    fun `LintingRule should use LintingRuleId correctly`() {
        val userId = "user123"
        val language = "kotlin"
        val configRules = mutableMapOf<String, Any>("maxLineLength" to 120)

        val lintingRule =
            LintingRule(
                userId = userId,
                setLanguage = language,
                configRules = configRules,
            )

        val lintingRuleId = LintingRuleId(userId, language)

        assertEquals(lintingRule.userId, lintingRuleId.userId)
        assertEquals(lintingRule.setLanguage, lintingRuleId.setLanguage)
    }

    @Test
    fun `should create LintingRuleId from LintingRule`() {
        val lintingRule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        val id = LintingRuleId(lintingRule.userId, lintingRule.setLanguage)

        assertEquals("user123", id.userId)
        assertEquals("kotlin", id.setLanguage)
    }

    @Test
    fun `should use LintingRuleId as composite key in map`() {
        val map = mutableMapOf<LintingRuleId, LintingRule>()

        val id = LintingRuleId("user123", "kotlin")
        val rule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        map[id] = rule

        assertEquals(rule, map[id])
        assertEquals(1, map.size)
    }

    @Test
    fun `should handle multiple rules for same user with different languages`() {
        val map = mutableMapOf<LintingRuleId, LintingRule>()
        val userId = "user123"

        val kotlinRule =
            LintingRule(
                userId = userId,
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120),
            )

        val javaRule =
            LintingRule(
                userId = userId,
                setLanguage = "java",
                configRules = mutableMapOf("maxLineLength" to 100),
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
        val rule =
            LintingRule(
                userId = "user123",
                setLanguage = "kotlin",
                configRules = mutableMapOf("maxLineLength" to 120, "enforceNamingConvention" to true),
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
