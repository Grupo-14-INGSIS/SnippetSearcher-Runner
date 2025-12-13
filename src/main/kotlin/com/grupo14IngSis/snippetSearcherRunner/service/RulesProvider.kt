package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry

interface RulesProvider {
    fun getFormattingRules(version: String?): LanguageRuleEntry

    fun getLintingRules(version: String?): LanguageRuleEntry
}
