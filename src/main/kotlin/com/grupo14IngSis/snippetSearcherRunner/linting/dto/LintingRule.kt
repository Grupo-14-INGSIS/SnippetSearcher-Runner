package com.grupo14IngSis.snippetSearcherRunner.linting.dto

class LintingRule (
    val name: String,
    val configurable: Boolean,
    val type: RuleParameterType,
    val default: String
)