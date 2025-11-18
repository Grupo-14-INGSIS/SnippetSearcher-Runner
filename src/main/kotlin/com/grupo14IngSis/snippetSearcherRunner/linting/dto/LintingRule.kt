package com.grupo14IngSis.snippetSearcherRunner.linting.dto

data class LintingRule (
    val name: String,
    val configurable: Boolean,
    val type: RuleParameterType,
    val default: String
)