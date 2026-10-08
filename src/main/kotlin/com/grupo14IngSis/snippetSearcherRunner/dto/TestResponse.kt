package com.grupo14IngSis.snippetSearcherRunner.dto

data class TestResponse(
    val actual: List<String>,
    val result: TestResult,
    val message: String,
)

enum class TestResult {
    SUCCESS,
    FAILED,
    ERROR,
}
