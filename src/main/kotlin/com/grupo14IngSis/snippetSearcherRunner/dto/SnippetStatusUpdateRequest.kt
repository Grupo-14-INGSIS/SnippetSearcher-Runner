package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetStatusUpdateRequest(
    val userId: String,
    val task: String,
    val status: Boolean,
)
