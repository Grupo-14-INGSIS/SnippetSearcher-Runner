package com.grupo14lngSis.snippetSearcherRunner.events

data class SnippetUpdatedEvent(
    val snippetId: String,
    val userId: String,
    val content: String,
    val language: String,
    val version: String,
)
