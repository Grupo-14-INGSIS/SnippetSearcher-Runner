package com.grupo14IngSis.snippetSearcherRunner.dto

data class GetSnippetResponse(
    val snippetId: String,
    val content: String,
    val name: String? = null,
)
