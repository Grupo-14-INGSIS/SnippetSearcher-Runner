package com.grupo14IngSis.snippetSearcherRunner.testing.dto

data class StartTestRequest(
    val snippetId: String,
    val snippet: String,
    val input: String,
    val expected: String
)
