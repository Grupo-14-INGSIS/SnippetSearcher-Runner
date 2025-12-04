package com.grupo14IngSis.snippetSearcherRunner.dto

data class TestRequest(
    val snippetId: String,
    val input: List<String>,
    val expected: String,
)
