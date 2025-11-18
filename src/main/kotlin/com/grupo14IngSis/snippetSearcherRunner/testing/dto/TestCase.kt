package com.grupo14IngSis.snippetSearcherRunner.testing.dto

data class TestCase(
    val id: String,
    val name: String,
    val input: List<String>,
    val expectedOutput: List<String>,
)
