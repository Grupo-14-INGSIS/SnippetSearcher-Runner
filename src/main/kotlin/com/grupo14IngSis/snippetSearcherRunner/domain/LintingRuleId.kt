package com.grupo14IngSis.snippetSearcherRunner.domain

import java.io.Serializable

data class LintingRuleId(
  val userId: String = "",
  val setLanguage: String = ""
): Serializable
