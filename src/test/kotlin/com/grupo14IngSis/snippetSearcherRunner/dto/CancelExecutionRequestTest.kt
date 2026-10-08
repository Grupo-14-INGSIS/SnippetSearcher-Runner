package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CancelExecutionRequestTest {
    @Test
    fun basicTestToReach80PercCoverage() {
        val request = CancelExecutionRequest("userId")
        assertEquals("userId", request.userId)
    }
}
