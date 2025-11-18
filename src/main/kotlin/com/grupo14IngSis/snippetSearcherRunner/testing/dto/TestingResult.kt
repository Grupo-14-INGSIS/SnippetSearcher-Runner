package com.grupo14lngSis.snippetSearcherRunner.testing.dto

data class TestingResult(
    val snippetId: String,
    val userId: String,
    val status: TestingStatus,
    val results: List<TestCaseResult>,
    val executedAt: String,
)

enum class TestingStatus {
    ALL_PASSED,
    SOME_FAILED,
    ERROR,
}

data class TestCaseResult(
    val testCaseId: String,
    val testCaseName: String,
    val passed: Boolean,
    val actualOutput: List<String>?,
    val errorMessage: String?,
)
