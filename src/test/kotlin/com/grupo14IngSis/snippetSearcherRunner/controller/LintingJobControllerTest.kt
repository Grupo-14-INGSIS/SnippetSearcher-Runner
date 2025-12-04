package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LintingJobControllerTest {
    private val lintingService: LintingService = mockk(relaxed = true)
    private val lintingJobController = LintingJobController(lintingService)

    @Test
    fun `getRules should return 200 with rules when found`() {
        val userId = "test-user"
        val language = "kotlin"
        val rules = mapOf("rule1" to "value1")
        every { lintingService.getRules(userId, language) } returns rules

        val response = lintingJobController.getRules(userId, language)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(rules, response.body)
    }

    @Test
    fun `getRules should return 404 when no rules are found`() {
        val userId = "test-user"
        val language = "kotlin"
        every { lintingService.getRules(userId, language) } returns emptyMap()

        val response = lintingJobController.getRules(userId, language)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(mapOf("message" to "User or language not found"), response.body)
    }

    @Test
    fun `editRules should return 204 when rules are updated`() {
        val userId = "test-user"
        val language = "kotlin"
        val request = mapOf("rule1" to "newValue")

        val response = lintingJobController.editRules(userId, language, request)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify { lintingService.updateRules(userId, language, request) }
    }

    @Test
    fun `editRules should return 404 on illegal argument exception`() {
        val userId = "test-user"
        val language = "kotlin"
        val request = mapOf("rule1" to "newValue")
        val errorMessage = "User not found"
        every { lintingService.updateRules(userId, language, request) } throws IllegalArgumentException(errorMessage)

        val response = lintingJobController.editRules(userId, language, request)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(mapOf("message" to errorMessage), response.body)
    }
}
