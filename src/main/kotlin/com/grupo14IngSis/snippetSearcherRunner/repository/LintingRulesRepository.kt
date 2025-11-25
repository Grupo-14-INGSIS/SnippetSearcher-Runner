package com.grupo14IngSis.snippetSearcherRunner.repository

import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRuleId
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LintingRulesRepository : JpaRepository<LintingRule, LintingRuleId>  {
  fun findByUserId(userId: String): List<LintingRule>

  @Transactional
  fun deleteByUserId(userId: String)
}