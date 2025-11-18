package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetExecutionStatusResponse(
    val jobId: String,
    val snippetId: String,
    val status: SnippetExecutionStatus
)
