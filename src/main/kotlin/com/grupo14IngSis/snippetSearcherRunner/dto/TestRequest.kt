package com.grupo14IngSis.snippetSearcherRunner.dto

data class TestRequest(
    val snippetId: String,
    val userId: String,
    val version: String,
    val environment: Map<String, String>,
    val input: List<String>,
    val expected: List<String>,
)
