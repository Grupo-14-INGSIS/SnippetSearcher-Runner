package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetExecutionRequest(
    val snippetId: String,
    val version: String? = null,
    val language: String = "printscript"
)