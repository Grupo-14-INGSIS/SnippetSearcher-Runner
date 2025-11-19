package com.grupo14IngSis.snippetSearcherRunner.testing.dto

data class TestStatusResponse(
    val jobId: String,
    val snippetId: String,
    val input: String,
    val expected: String,
    val actual: String,
    val status: TestStatus
)
