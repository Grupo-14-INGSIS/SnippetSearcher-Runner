package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestRequestTest {
    @Test
    fun `create instance and access properties`() {
        val request =
            TestRequest(
                snippetId = "123",
                input = listOf("arg1", "arg2"),
                expected = "result",
            )

        assertEquals("123", request.snippetId)
        assertEquals(listOf("arg1", "arg2"), request.input)
        assertEquals("result", request.expected)
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val req1 = TestRequest("123", listOf("a"), "out")
        val req2 = TestRequest("123", listOf("a"), "out")
        val req3 = TestRequest("456", listOf("b"), "other")

        assertEquals(req1, req2)
        assertEquals(req1.hashCode(), req2.hashCode())
        assertNotEquals(req1, req3)
    }

    @Test
    fun `copy creates new instance with modifications`() {
        val original = TestRequest("123", listOf("a"), "out")
        val copy = original.copy(expected = "newOut")

        assertEquals("123", copy.snippetId)
        assertEquals(listOf("a"), copy.input)
        assertEquals("newOut", copy.expected)
        assertNotEquals(original, copy)
    }

    @Test
    fun `destructuring works`() {
        val request = TestRequest("123", listOf("a", "b"), "out")
        val (id, input, expected) = request

        assertEquals("123", id)
        assertEquals(listOf("a", "b"), input)
        assertEquals("out", expected)
    }

    @Test
    fun `toString contains class name and properties`() {
        val request = TestRequest("123", listOf("a"), "out")
        val str = request.toString()

        assertTrue(str.contains("TestRequest"))
        assertTrue(str.contains("123"))
        assertTrue(str.contains("out"))
    }
}
