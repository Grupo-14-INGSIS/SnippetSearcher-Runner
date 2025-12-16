package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class FormattingServiceTest {
    private lateinit var repository: FormattingRulesRepository
    private lateinit var formattingPlugin: FormattingPlugin
    private lateinit var formattingService: FormattingService

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        formattingPlugin = mockk(relaxed = true)
        formattingService = FormattingService(repository, formattingPlugin)
    }

    @Test
    fun `getRules should return rules when found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = FormattingRuleId(userId, language)
        val configRules = mutableMapOf<String, Any>("rule1" to "value1")
        val formattingRule = FormattingRule(userId, language, configRules)
        every { repository.findById(id) } returns Optional.of(formattingRule)

        val rules = formattingService.getRules(userId, language)

        assertEquals(configRules, rules)
    }

    @Test
    fun `getRules should return empty map when not found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = FormattingRuleId(userId, language)
        every { repository.findById(id) } returns Optional.empty()

        val rules = formattingService.getRules(userId, language)

        assertEquals(emptyMap<String, Any>(), rules)
    }

    @Test
    fun `updateRules should update and save rules`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = FormattingRuleId(userId, language)
        val newRules = mapOf("rule2" to "newValue")
        val existingRules = mutableMapOf<String, Any>("rule1" to "value1")
        val formattingRule = FormattingRule(userId, language, existingRules)

        every { repository.findById(id) } returns Optional.of(formattingRule)
        every { repository.save(any()) } returns formattingRule

        formattingService.updateRules(userId, language, newRules)

        verify { repository.save(formattingRule) }
        assertEquals("newValue", formattingRule.configRules?.get("rule2"))
    }

    @Test
    fun `updateRules should throw exception when not found`() {
        val userId = "test-user"
        val language = "kotlin"
        val id = FormattingRuleId(userId, language)
        val newRules = mapOf("rule2" to "newValue")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            formattingService.updateRules(userId, language, newRules)
        }
    }
}
