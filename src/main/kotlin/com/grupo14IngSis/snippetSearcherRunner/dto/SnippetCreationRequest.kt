package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetCreationRequest(
    val userId: String,
    val name: String,
    val language: String,
    val snippet: String,
)
