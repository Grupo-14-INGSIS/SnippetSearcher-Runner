package com.grupo14IngSis.snippetSearcherRunner.repository

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface FormattingRulesRepository : JpaRepository<FormattingRule, FormattingRuleId> {
    // Antes era findByUserId(): List<LintingRule>. Con el tipo equivocado, Spring Data lo armaba como
    // `new LintingRule(..., f.configRules)`, y desde que configRules dejó de ser columna (sin JSONB) fallaba
    // siempre: ningún usuario nuevo se podía crear en el Runner.
    fun existsByUserId(userId: String): Boolean

    @Transactional
    fun deleteByUserId(userId: String)
}
