package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRuleId
import com.grupo14IngSis.snippetSearcherRunner.repository.LintingRulesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class LintingServiceTest {
    private lateinit var repository: LintingRulesRepository
    private lateinit var lintingService: LintingService

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        lintingService = LintingService(repository)
    }

    @Test
    fun `getRules should return rules when found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = LintingRuleId(userId, language)
        val configRules = mutableMapOf<String, Any>("rule1" to "value1")
        val lintingRule = LintingRule(userId, language, configRules)
        every { repository.findById(id) } returns Optional.of(lintingRule)

        val rules = lintingService.getRules(userId, language)

        assertEquals(configRules, rules)
    }

    @Test
    fun `getRules should return empty map when not found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = LintingRuleId(userId, language)
        every { repository.findById(id) } returns Optional.empty()

        val rules = lintingService.getRules(userId, language)

        assertEquals(emptyMap<String, Any>(), rules)
    }

    @Test
    fun `updateRules should update and save rules`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = LintingRuleId(userId, language)
        val newRules = mapOf("rule2" to "newValue")
        val existingRules = mutableMapOf<String, Any>("rule1" to "value1")
        val lintingRule = LintingRule(userId, language, existingRules)

        every { repository.findById(id) } returns Optional.of(lintingRule)
        every { repository.save(any()) } returns lintingRule

        lintingService.updateRules(userId, language, newRules)

        verify { repository.save(lintingRule) }
        assertEquals("newValue", lintingRule.configRules?.get("rule2"))
    }

    @Test
    fun `updateRules should throw exception when not found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = LintingRuleId(userId, language)
        val newRules = mapOf("rule2" to "newValue")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            lintingService.updateRules(userId, language, newRules)
        }
    }
}
