package com.grupo14IngSis.snippetSearcherRunner.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FormattingRuleIdTest {

    @Test
    fun `should create FormattingRuleId with all parameters`() {
        val id = FormattingRuleId(
            userId = "user123",
            setLanguage = "kotlin"
        )

        assertEquals("user123", id.userId)
        assertEquals("kotlin", id.setLanguage)
    }

    @Test
    fun `should create FormattingRuleId with default values`() {
        val id = FormattingRuleId()

        assertEquals("", id.userId)
        assertEquals("", id.setLanguage)
    }

    @Test
    fun `should be equal when userId and setLanguage are the same`() {
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user123", "kotlin")

        assertEquals(id1, id2)
    }

    @Test
    fun `should not be equal when userId is different`() {
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user456", "kotlin")

        assertNotEquals(id1, id2)
    }

    @Test
    fun `should not be equal when setLanguage is different`() {
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user123", "java")

        assertNotEquals(id1, id2)
    }

    @Test
    fun `should have same hashCode for equal objects`() {
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user123", "kotlin")

        assertEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun `should have different hashCode for different objects`() {
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user456", "java")

        assertNotEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun `should be serializable`() {
        val id = FormattingRuleId("user123", "kotlin")

        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(id)
        objectOutputStream.close()

        val byteArray = byteArrayOutputStream.toByteArray()
        assertTrue(byteArray.isNotEmpty())
    }

    @Test
    fun `should deserialize correctly`() {
        val original = FormattingRuleId("user123", "kotlin")

        // Serialize
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(original)
        objectOutputStream.close()

        // Deserialize
        val byteArray = byteArrayOutputStream.toByteArray()
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val deserialized = objectInputStream.readObject() as FormattingRuleId

        assertEquals(original, deserialized)
        assertEquals(original.userId, deserialized.userId)
        assertEquals(original.setLanguage, deserialized.setLanguage)
    }

    @Test
    fun `should support copy with modifications`() {
        val original = FormattingRuleId("user123", "kotlin")
        val modified = original.copy(setLanguage = "java")

        assertEquals("user123", modified.userId)
        assertEquals("java", modified.setLanguage)
        assertNotEquals(original, modified)
    }

    @Test
    fun `should handle special characters in userId`() {
        val id = FormattingRuleId("user-123_test@example", "kotlin")

        assertEquals("user-123_test@example", id.userId)
    }

    @Test
    fun `should handle special characters in setLanguage`() {
        val id = FormattingRuleId("user123", "C++")

        assertEquals("C++", id.setLanguage)
    }

    @Test
    fun `should work as map key`() {
        val map = mutableMapOf<FormattingRuleId, String>()
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user123", "kotlin")

        map[id1] = "value1"
        map[id2] = "value2"

        assertEquals(1, map.size)
        assertEquals("value2", map[id1])
    }

    @Test
    fun `should work in sets`() {
        val set = mutableSetOf<FormattingRuleId>()
        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user123", "kotlin")
        val id3 = FormattingRuleId("user456", "java")

        set.add(id1)
        set.add(id2)
        set.add(id3)

        assertEquals(2, set.size)
    }

    @Test
    fun `should have proper toString representation`() {
        val id = FormattingRuleId("user123", "kotlin")
        val toString = id.toString()

        assertTrue(toString.contains("user123"))
        assertTrue(toString.contains("kotlin"))
    }

    @Test
    fun `should handle empty strings`() {
        val id = FormattingRuleId("", "")

        assertEquals("", id.userId)
        assertEquals("", id.setLanguage)
    }

    @Test
    fun `should be usable with different languages`() {
        val languages = listOf("kotlin", "java", "python", "javascript", "typescript")
        val ids = languages.map { FormattingRuleId("user123", it) }

        assertEquals(5, ids.size)
        assertEquals(5, ids.toSet().size)
    }

    @Test
    fun `should handle long userId`() {
        val longUserId = "a".repeat(100)
        val id = FormattingRuleId(longUserId, "kotlin")

        assertEquals(longUserId, id.userId)
        assertEquals(100, id.userId.length)
    }

    @Test
    fun `should handle long setLanguage`() {
        val longLanguage = "l".repeat(50)
        val id = FormattingRuleId("user123", longLanguage)

        assertEquals(longLanguage, id.setLanguage)
        assertEquals(50, id.setLanguage.length)
    }

    @Test
    fun `data class components should match fields`() {
        val id = FormattingRuleId("user123", "kotlin")
        val (userId, setLanguage) = id

        assertEquals("user123", userId)
        assertEquals("kotlin", setLanguage)
    }
}

class FormattingRuleAndIdIntegrationTest {

    @Test
    fun `FormattingRule should use FormattingRuleId correctly`() {
        val userId = "user123"
        val language = "kotlin"
        val configRules = mutableMapOf<String, Any>("indentSize" to 4)

        val formattingRule = FormattingRule(
            userId = userId,
            setLanguage = language,
            configRules = configRules
        )

        val formattingRuleId = FormattingRuleId(userId, language)

        assertEquals(formattingRule.userId, formattingRuleId.userId)
        assertEquals(formattingRule.setLanguage, formattingRuleId.setLanguage)
    }

    @Test
    fun `should create FormattingRuleId from FormattingRule`() {
        val formattingRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        val id = FormattingRuleId(formattingRule.userId, formattingRule.setLanguage)

        assertEquals("user123", id.userId)
        assertEquals("kotlin", id.setLanguage)
    }

    @Test
    fun `multiple FormattingRules with same userId and language should have equal ids`() {
        val rule1 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        val rule2 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("lineLength" to 120)
        )

        val id1 = FormattingRuleId(rule1.userId, rule1.setLanguage)
        val id2 = FormattingRuleId(rule2.userId, rule2.setLanguage)

        assertEquals(id1, id2)
    }

    @Test
    fun `should use FormattingRuleId as composite key in map`() {
        val map = mutableMapOf<FormattingRuleId, FormattingRule>()

        val id = FormattingRuleId("user123", "kotlin")
        val rule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        map[id] = rule

        assertEquals(rule, map[id])
        assertEquals(1, map.size)
    }

    @Test
    fun `should handle multiple rules for same user with different languages`() {
        val map = mutableMapOf<FormattingRuleId, FormattingRule>()
        val userId = "user123"

        val kotlinRule = FormattingRule(
            userId = userId,
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        val javaRule = FormattingRule(
            userId = userId,
            setLanguage = "java",
            configRules = mutableMapOf("indentSize" to 2)
        )

        map[FormattingRuleId(userId, "kotlin")] = kotlinRule
        map[FormattingRuleId(userId, "java")] = javaRule

        assertEquals(2, map.size)
        assertEquals(4, map[FormattingRuleId(userId, "kotlin")]?.configRules?.get("indentSize"))
        assertEquals(2, map[FormattingRuleId(userId, "java")]?.configRules?.get("indentSize"))
    }

    @Test
    fun `should handle multiple rules for different users with same language`() {
        val map = mutableMapOf<FormattingRuleId, FormattingRule>()
        val language = "kotlin"

        val user1Rule = FormattingRule(
            userId = "user123",
            setLanguage = language,
            configRules = mutableMapOf("indentSize" to 4)
        )

        val user2Rule = FormattingRule(
            userId = "user456",
            setLanguage = language,
            configRules = mutableMapOf("indentSize" to 2)
        )

        map[FormattingRuleId("user123", language)] = user1Rule
        map[FormattingRuleId("user456", language)] = user2Rule

        assertEquals(2, map.size)
        assertEquals(4, map[FormattingRuleId("user123", language)]?.configRules?.get("indentSize"))
        assertEquals(2, map[FormattingRuleId("user456", language)]?.configRules?.get("indentSize"))
    }

    @Test
    fun `should retrieve FormattingRule using FormattingRuleId`() {
        val storage = mutableMapOf<FormattingRuleId, FormattingRule>()

        val id = FormattingRuleId("user123", "kotlin")
        val rule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4, "lineLength" to 120)
        )

        storage[id] = rule

        val retrieved = storage[id]
        assertNotNull(retrieved)
        assertEquals(rule.userId, retrieved?.userId)
        assertEquals(rule.setLanguage, retrieved?.setLanguage)
        assertEquals(2, retrieved?.configRules?.size)
    }

    @Test
    fun `should update FormattingRule using FormattingRuleId`() {
        val storage = mutableMapOf<FormattingRuleId, FormattingRule>()

        val id = FormattingRuleId("user123", "kotlin")
        val originalRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 4)
        )

        storage[id] = originalRule

        val updatedRule = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf("indentSize" to 2, "lineLength" to 100)
        )

        storage[id] = updatedRule

        assertEquals(1, storage.size)
        assertEquals(2, storage[id]?.configRules?.get("indentSize"))
        assertEquals(2, storage[id]?.configRules?.size)
    }

    @Test
    fun `should check existence of FormattingRule using FormattingRuleId`() {
        val storage = mutableMapOf<FormattingRuleId, FormattingRule>()

        val id1 = FormattingRuleId("user123", "kotlin")
        val id2 = FormattingRuleId("user456", "java")

        val rule1 = FormattingRule(
            userId = "user123",
            setLanguage = "kotlin",
            configRules = mutableMapOf()
        )

        storage[id1] = rule1

        assertTrue(storage.containsKey(id1))
        assertFalse(storage.containsKey(id2))
    }

    @Test
    fun `should filter rules by userId using FormattingRuleId`() {
        val storage = mutableMapOf<FormattingRuleId, FormattingRule>()

        storage[FormattingRuleId("user123", "kotlin")] = FormattingRule("user123", "kotlin")
        storage[FormattingRuleId("user123", "java")] = FormattingRule("user123", "java")
        storage[FormattingRuleId("user456", "kotlin")] = FormattingRule("user456", "kotlin")

        val user123Rules = storage.filter { it.key.userId == "user123" }

        assertEquals(2, user123Rules.size)
    }

    @Test
    fun `should serialize and deserialize FormattingRuleId with FormattingRule`() {
        val id = FormattingRuleId("user123", "kotlin")

        // Serialize ID
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(id)
        objectOutputStream.close()

        // Deserialize ID
        val byteArray = byteArrayOutputStream.toByteArray()
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val deserializedId = objectInputStream.readObject() as FormattingRuleId

        assertEquals(id, deserializedId)

        // Verify it can be used to retrieve a rule
        val storage = mutableMapOf<FormattingRuleId, FormattingRule>()
        val rule = FormattingRule("user123", "kotlin", mutableMapOf("indentSize" to 4))
        storage[deserializedId] = rule

        assertEquals(rule, storage[deserializedId])
    }
}