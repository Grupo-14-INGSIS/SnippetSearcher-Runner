package com.grupo14IngSis.snippetSearcherRunner.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExecutionRequestTest {
    @Test
    fun basicTestToReach80PercCoverage() {
        val request = ExecutionRequest("userId", mapOf("1" to "Apples", "2" to "Car"), "1.23.4")
        assertEquals("userId", request.userId)
        assertEquals("Apples", request.environment["1"])
        assertEquals("Car", request.environment["2"])
        assertEquals("1.23.4", request.version)
    }
}
