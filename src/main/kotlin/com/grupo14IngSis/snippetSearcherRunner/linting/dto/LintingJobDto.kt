package com.grupo14IngSis.snippetSearcherRunner.linting.dto

import java.time.LocalDateTime

data class LintingJob(
    val id: String,
    val ruleId: String,
    val userId: String,
    val totalSnippets: Int,
    val processedSnippets: Int = 0,
    val passedSnippets: Int = 0, // ← Snippets que pasaron el linting
    val failedSnippets: List<String> = emptyList(), // ← Snippets que fallaron
    val lintingResults: Map<String, LintingResult> = emptyMap(), // ← Resultados por snippet
    val status: LintingJobStatus,
    val createdAt: LocalDateTime,
    val lastProcessedSnippetId: String? = null,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
)

data class LintingResult(
    val snippetId: String,
    val passed: Boolean,
    val violations: List<LintViolation> = emptyList(),
    val errorMessage: String? = null,
)

data class LintViolation(
    val rule: String,
    val message: String,
    val line: Int,
    val column: Int,
)

data class SnippetLintResult(
    val snippetId: String,
    val success: Boolean,
    val passed: Boolean = false, // ← Si pasó el linting
    val violations: List<LintViolation> = emptyList(),
    val error: String? = null,
)
