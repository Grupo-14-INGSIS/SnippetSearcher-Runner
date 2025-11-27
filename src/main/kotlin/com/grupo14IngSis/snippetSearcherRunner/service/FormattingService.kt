package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FormattingService(
  private val repository: FormattingRulesRepository,
) {

  fun getRules(userId: String, language: String): Map<String, Any> {
    val id = FormattingRuleId(userId, language)
    val rules: FormattingRule = repository.findById(id).orElse(null) ?: return mapOf()
    return rules.configRules!!
  }

  @Transactional
  fun updateRules(userId: String, language: String, newRules: Map<String, Any>) {
    val id = FormattingRuleId(userId, language)
    val existing: FormattingRule = repository.findById(id).orElseThrow {
      IllegalArgumentException("User not found or language not found")
    }
    existing.configRules?.putAll(newRules)
    repository.save(existing)
  }
}