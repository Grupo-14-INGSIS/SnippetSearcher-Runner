package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UserCreationRequestTest {

    private val sampleEntry = LanguageRuleEntry(
        language = "Kotlin",
        rules = mapOf("indent" to 4, "maxLineLength" to 120)
    )

    @Test
    fun `create instance and access properties`() {
        val request = UserCreationRequest(languages = listOf(sampleEntry))

        assertEquals(1, request.languages.size)
        assertEquals("Kotlin", request.languages[0].language)
        assertEquals(4, request.languages[0].rules["indent"])
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val req1 = UserCreationRequest(listOf(sampleEntry))
        val req2 = UserCreationRequest(listOf(sampleEntry))
        val req3 = UserCreationRequest(emptyList())

        assertEquals(req1, req2)
        assertEquals(req1.hashCode(), req2.hashCode())
        assertNotEquals(req1, req3)
    }

    @Test
    fun `copy creates new instance with modifications`() {
        val original = UserCreationRequest(listOf(sampleEntry))
        val newEntry = LanguageRuleEntry("Java", mapOf("indent" to 2))
        val copy = original.copy(languages = listOf(newEntry))

        assertEquals(1, copy.languages.size)
        assertEquals("Java", copy.languages[0].language)
        assertNotEquals(original, copy)
    }

    @Test
    fun `destructuring works`() {
        val request = UserCreationRequest(listOf(sampleEntry))
        val (languages) = request

        assertEquals("Kotlin", languages[0].language)
    }

    @Test
    fun `toString contains class name and properties`() {
        val request = UserCreationRequest(listOf(sampleEntry))
        val str = request.toString()

        assertTrue(str.contains("UserCreationRequest"))
        assertTrue(str.contains("Kotlin"))
        assertTrue(str.contains("indent"))
    }
}