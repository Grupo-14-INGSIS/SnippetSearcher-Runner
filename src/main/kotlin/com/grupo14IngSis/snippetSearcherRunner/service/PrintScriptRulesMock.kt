package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry

class PrintScriptRulesMock {
  companion object {
    fun lintingRules(): LanguageRuleEntry {
      val rules = mapOf("rule1" to true, "rule2" to false, "rule3" to 3)
      return LanguageRuleEntry("printscript", rules)
    }

    fun formattingRules(): LanguageRuleEntry {
      val rules = mapOf("rule1" to true, "rule2" to false, "rule3" to 3)
      return LanguageRuleEntry("printscript", rules)
    }
  }
}