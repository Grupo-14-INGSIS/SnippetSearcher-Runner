package com.grupo14IngSis.snippetSearcherRunner.linting.dto

data class LintingJobRequest (
    val snippetId: String,
    val snippet: String,
    val rules: Map<String, Any>
    )