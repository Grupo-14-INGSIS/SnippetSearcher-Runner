package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.model.TestCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class TestService(
    private val testExecutionService: TestExecutionService,
    private val snippetService: SnippetService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Almacenamiento en memoria
    private val tests = ConcurrentHashMap<String, TestCase>()
    private val testsBySnippet = ConcurrentHashMap<Long, MutableList<String>>()

    fun createTest(
        snippetId: Long,
        userId: String,
        request: CreateTestRequest,
    ): TestCaseDto {
        logger.info("Creating test '${request.name}' for snippet $snippetId")

        // Verificar que el snippet existe y el usuario tiene acceso
        try {
            snippetService.getSnippetById(snippetId, userId)
        } catch (e: Exception) {
            throw TestException("Snippet not found or access denied: $snippetId")
        }

        val testId = UUID.randomUUID().toString()
        val test =
            TestCase(
                id = testId,
                snippetId = snippetId,
                name = request.name,
                inputs = request.inputs,
                expectedOutputs = request.expectedOutputs,
            )

        tests[testId] = test
        testsBySnippet.computeIfAbsent(snippetId) { mutableListOf() }.add(testId)

        logger.info("Test created with ID: $testId")
        return test.toDto()
    }

    fun getTestsBySnippet(
        snippetId: Long,
        userId: String,
    ): List<TestCaseDto> {
        logger.debug("Fetching tests for snippet $snippetId")

        // Verificar acceso al snippet
        try {
            snippetService.getSnippetById(snippetId, userId)
        } catch (e: Exception) {
            throw TestException("Snippet not found or access denied: $snippetId")
        }

        val testIds = testsBySnippet[snippetId] ?: emptyList()
        return testIds.mapNotNull { tests[it]?.toDto() }
    }

    fun getTest(
        testId: String,
        userId: String,
    ): TestCaseDto {
        logger.debug("Fetching test $testId")

        val test = tests[testId] ?: throw TestException("Test not found: $testId")

        // Verificar acceso al snippet
        try {
            snippetService.getSnippetById(test.snippetId, userId)
        } catch (e: Exception) {
            throw TestException("Access denied to test: $testId")
        }

        return test.toDto()
    }

    fun updateTest(
        testId: String,
        userId: String,
        request: UpdateTestRequest,
    ): TestCaseDto {
        logger.info("Updating test $testId")

        val existing = tests[testId] ?: throw TestException("Test not found: $testId")

        // Verificar acceso al snippet
        try {
            snippetService.getSnippetById(existing.snippetId, userId)
        } catch (e: Exception) {
            throw TestException("Access denied to test: $testId")
        }

        val updated =
            existing.copy(
                name = request.name ?: existing.name,
                inputs = request.inputs ?: existing.inputs,
                expectedOutputs = request.expectedOutputs ?: existing.expectedOutputs,
                updatedAt = LocalDateTime.now(),
            )

        tests[testId] = updated
        logger.info("Test $testId updated")
        return updated.toDto()
    }

    fun deleteTest(
        testId: String,
        userId: String,
    ) {
        logger.info("Deleting test $testId")

        val test = tests[testId] ?: throw TestException("Test not found: $testId")

        // Verificar acceso al snippet
        try {
            snippetService.getSnippetById(test.snippetId, userId)
        } catch (e: Exception) {
            throw TestException("Access denied to test: $testId")
        }

        tests.remove(testId)
        testsBySnippet[test.snippetId]?.remove(testId)

        logger.info("Test $testId deleted")
    }

    fun runTest(
        snippetId: Long,
        testId: String,
        userId: String,
    ): TestResultDto {
        logger.info("Running test $testId for snippet $snippetId")

        val test = tests[testId] ?: throw TestException("Test not found: $testId")

        if (test.snippetId != snippetId) {
            throw TestException("Test does not belong to this snippet")
        }

        val snippet =
            try {
                snippetService.getSnippetById(snippetId, userId)
            } catch (e: Exception) {
                throw TestException("Snippet not found or access denied: $snippetId")
            }

        return testExecutionService.executeTest(snippet, test)
    }

    fun runAllTests(
        snippetId: Long,
        userId: String,
    ): List<TestResultDto> {
        logger.info("Running all tests for snippet $snippetId")

        val snippet =
            try {
                snippetService.getSnippetById(snippetId, userId)
            } catch (e: Exception) {
                throw TestException("Snippet not found or access denied: $snippetId")
            }

        val testIds = testsBySnippet[snippetId] ?: emptyList()

        return testIds.mapNotNull { testId ->
            tests[testId]?.let { test ->
                testExecutionService.executeTest(snippet, test)
            }
        }
    }

    private fun TestCase.toDto() =
        TestCaseDto(
            id = id,
            snippetId = snippetId,
            name = name,
            inputs = inputs,
            expectedOutputs = expectedOutputs,
        )
}

class TestException(message: String) : RuntimeException(message)
