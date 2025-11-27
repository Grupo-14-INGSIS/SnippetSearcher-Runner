package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry
import com.grupo14IngSis.snippetSearcherRunner.dto.UserCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import com.grupo14IngSis.snippetSearcherRunner.repository.LintingRulesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTest {
    private lateinit var formattingRepository: FormattingRulesRepository
    private lateinit var lintingRepository: LintingRulesRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        formattingRepository = mockk(relaxed = true)
        lintingRepository = mockk(relaxed = true)
        userService = UserService(formattingRepository, lintingRepository)
    }

    @Test
    fun `check should return true when user exists`() {
        val userId = "test-user"
        every { formattingRepository.findByUserId(userId) } returns listOf(mockk())

        assertTrue(userService.check(userId))
    }

    @Test
    fun `check should return false when user does not exist`() {
        val userId = "test-user"
        every { formattingRepository.findByUserId(userId) } returns emptyList()

        assertFalse(userService.check(userId))
    }

    @Test
    fun `addUser should save formatting and linting rules`() {
        val userId = "test-user"
        val formattingRules = UserCreationRequest(listOf(LanguageRuleEntry("kotlin", mapOf("rule1" to "value1"))))
        val lintingRules = UserCreationRequest(listOf(LanguageRuleEntry("kotlin", mapOf("rule2" to "value2"))))

        val formattingRuleSlot = slot<FormattingRule>()
        val lintingRuleSlot = slot<LintingRule>()

        every { formattingRepository.save(capture(formattingRuleSlot)) } returns mockk()
        every { lintingRepository.save(capture(lintingRuleSlot)) } returns mockk()

        userService.addUser(userId, formattingRules, lintingRules)

        assertEquals(userId, formattingRuleSlot.captured.userId)
        assertEquals("kotlin", formattingRuleSlot.captured.setLanguage)
        assertEquals("value1", formattingRuleSlot.captured.configRules?.get("rule1"))

        assertEquals(userId, lintingRuleSlot.captured.userId)
        assertEquals("kotlin", lintingRuleSlot.captured.setLanguage)
        assertEquals("value2", lintingRuleSlot.captured.configRules?.get("rule2"))
    }

    @Test
    fun `deleteUser should delete formatting and linting rules`() {
        val userId = "test-user"

        userService.deleteUser(userId)

        verify { formattingRepository.deleteByUserId(userId) }
        verify { lintingRepository.deleteByUserId(userId) }
    }
}
