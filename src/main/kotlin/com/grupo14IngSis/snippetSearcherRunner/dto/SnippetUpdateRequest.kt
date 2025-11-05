package com.grupo14IngSis.snippetSearcherRunner.dto

data class SnippetUpdateRequest(
    val name: String?,
    val description: String?,
    val language: String?,
    val version: String?,
    val content: String?,
)
