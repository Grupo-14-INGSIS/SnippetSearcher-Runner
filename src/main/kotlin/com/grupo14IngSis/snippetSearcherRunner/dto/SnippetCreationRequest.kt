package com.grupo14IngSis.snippetSearcherRunner.dto

/** Alta del código de un snippet. Los metadatos y el dueño los registra App, no el Runner. */
data class SnippetCreationRequest(
    val name: String,
    val language: String,
    val snippet: String,
    val version: String? = "1.1",
)
