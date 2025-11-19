package com.grupo14IngSis.snippetSearcherRunner.formatting.dto

data class FormattingJobStatusResponse(
    val jobId: String,
    val snippetId: String,
    val status: FormattingJobStatus
)
