package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRuleId
import com.grupo14IngSis.snippetSearcherRunner.repository.LintingRulesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LintingService(
    private val repository: LintingRulesRepository,
) {
    fun getRules(
        userId: String,
        language: String,
    ): Map<String, Any> {
        val id = LintingRuleId(userId, language)
        val rules: LintingRule = repository.findById(id).orElse(null) ?: return mapOf()
        return rules.configRules!!
    }

    @Transactional
    fun updateRules(
        userId: String,
        language: String,
        newRules: Map<String, Any>,
    ) {
        val id = LintingRuleId(userId, language)
        val existing: LintingRule =
            repository.findById(id).orElseThrow {
                IllegalArgumentException("User not found or language not found")
            }
        existing.configRules?.putAll(newRules)
        repository.save(existing)
    }
}
