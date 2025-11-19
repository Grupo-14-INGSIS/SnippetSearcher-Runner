package com.grupo14IngSis.snippetSearcherRunner.linting.dto

data class LintingJobStatusResponse(
    val jobId: String,
    val snippetId: String,
    val jobStatus: LintingJobStatus
)