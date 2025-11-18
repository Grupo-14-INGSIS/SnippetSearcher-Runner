package com.grupo14IngSis.snippetSearcherRunner.dto

data class FormattingRuleDto(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val category: String,
)

data class FormattingConfigDto(
    val userId: String,
    val rules: List<FormattingRuleDto>,
    val lastUpdated: String,
)

data class UpdateRuleRequest(
    val ruleId: String,
    val enabled: Boolean,
)

data class BulkUpdateRulesRequest(
    val rules: Map<String, Boolean>, // ruleId -> enabled
)

data class FormattingConfigResponse(
    val success: Boolean,
    val message: String,
    val config: FormattingConfigDto?,
)
