package com.grupo14IngSis.snippetSearcherRunner.dto

data class TestCaseDto(
    val id: String,
    val snippetId: Long,
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>
)

data class CreateTestRequest(
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>
)

data class UpdateTestRequest(
    val name: String?,
    val inputs: List<String>?,
    val expectedOutputs: List<String>?
)

data class TestResultDto(
    val testId: String,
    val testName: String,
    val passed: Boolean,
    val executionSteps: List<ExecutionStep>,
    val error: String?
)

data class ExecutionStep(
    val stepNumber: Int,
    val type: StepType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StepType {
    INPUT,
    OUTPUT,
    ERROR
}