package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetCreationRequest(
    val userId: String,
    val language: String,
    val snippet: String,
)
