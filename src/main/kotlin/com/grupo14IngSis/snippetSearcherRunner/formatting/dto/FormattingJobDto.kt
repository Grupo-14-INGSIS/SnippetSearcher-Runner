package com.grupo14IngSis.snippetSearcherRunner.formatting.dto

import java.util.UUID

data class FormattingJob(
    val id: UUID?,
    val ruleId: String, // nombre como identificador de la rule
    val userId: Int,
    val totalSnippets: Int,
    val processedSnippets: Int = 0,
    val failedSnippets: List<String> = emptyList(),
    val status: FormattingJobStatus,
    val errorMessage: String? = null,
)

data class SnippetFormatResult(
    val snippetId: String,
    val success: Boolean,
    val formattedContent: String? = null,
    val error: String? = null,
)
