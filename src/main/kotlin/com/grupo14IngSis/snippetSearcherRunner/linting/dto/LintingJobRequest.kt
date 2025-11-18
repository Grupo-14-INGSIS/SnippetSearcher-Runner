package com.grupo14IngSis.snippetSearcherRunner.linting.dto

class LintingJobRequest (
    val snippetId: String,
    val snippet: String,
    val rules: Map<String, Any>
    )