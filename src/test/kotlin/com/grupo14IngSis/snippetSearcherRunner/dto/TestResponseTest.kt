package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestResponseTest {
    @Test
    fun `test getters`() {
        val response = TestResponse("actual", TestResult.PASSED)
        assertEquals("actual", response.actual)
        assertEquals(TestResult.PASSED, response.result)
    }
}
