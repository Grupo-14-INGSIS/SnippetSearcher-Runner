package com.grupo14IngSis.snippetSearcherRunner.dto

/** Código nuevo de un snippet. El linteo y los tests después de guardarlo los dispara App. */
data class SnippetUpdateRequest(
    val snippet: String,
    val version: String? = null,
)
