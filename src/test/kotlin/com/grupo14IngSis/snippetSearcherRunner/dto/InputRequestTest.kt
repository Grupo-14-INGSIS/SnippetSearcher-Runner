package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InputRequestTest {
    @Test
    fun basicTestToReach80PercCoverage() {
        val request = InputRequest("input", "userId")
        assertEquals("input", request.userId)
        assertEquals("userId", request.input)
    }
}
