package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import com.grupo14IngSis.snippetSearcherRunner.dto.UserCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import com.grupo14IngSis.snippetSearcherRunner.repository.LintingRulesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val formattingRepository: FormattingRulesRepository,
    private val lintingRepository: LintingRulesRepository,
) {
    fun check(userId: String): Boolean {
        val user = formattingRepository.findByUserId(userId)
        return user.isNotEmpty()
    }

    @Transactional
    fun addUser(
        userId: String,
        formattingRules: UserCreationRequest,
        lintingRules: UserCreationRequest,
    ) {
        for (language in formattingRules.languages) {
            val rules = mutableMapOf<String, Any>()
            rules.putAll(language.rules)
            val formattingRuleEntry =
                FormattingRule(
                    userId,
                    language.language,
                    rules,
                )
            formattingRepository.save(formattingRuleEntry)
        }
        for (language in lintingRules.languages) {
            val rules = mutableMapOf<String, Any>()
            rules.putAll(language.rules)
            val lintingRuleEntry =
                LintingRule(
                    userId,
                    language.language,
                    rules,
                )
            lintingRepository.save(lintingRuleEntry)
        }
    }

    @Transactional
    fun deleteUser(userId: String) {
        formattingRepository.deleteByUserId(userId)
        lintingRepository.deleteByUserId(userId)
    }
}
