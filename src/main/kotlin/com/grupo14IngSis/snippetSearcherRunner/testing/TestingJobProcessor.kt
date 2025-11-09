//package com.grupo14lngSis.snippetSearcherRunner.testing
//
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetServiceClient
//import com.grupo14lngSis.snippetSearcherRunner.testing.dto.*
//import org.springframework.stereotype.Component
//
//@Component
//class TestingJobProcessor(
//    private val printScriptClient: SnippetServiceClient,
//) {
//    fun processTestingJob(request: TestingRequest): TestingResult {
//        val results = mutableListOf<TestCaseResult>()
//
//        request.testCases.forEach { testCase ->
//            try {
//                val actualOutput =
//                    printScriptClient.executeSnippet(
//                        content = request.content,
//                        language = request.language,
//                        inputs = testCase.input,
//                    )
//
//                val passed = actualOutput == testCase.expectedOutput
//
//                results.add(
//                    TestCaseResult(
//                        testCaseId = testCase.id,
//                        testCaseName = testCase.name,
//                        passed = passed,
//                        actualOutput = actualOutput,
//                        errorMessage = if (!passed) "Expected ${testCase.expectedOutput}, got $actualOutput" else null,
//                    ),
//                )
//            } catch (e: Exception) {
//                results.add(
//                    TestCaseResult(
//                        testCaseId = testCase.id,
//                        testCaseName = testCase.name,
//                        passed = false,
//                        actualOutput = null,
//                        errorMessage = e.message,
//                    ),
//                )
//            }
//        }
//
//        val status =
//            when {
//                results.all { it.passed } -> TestingStatus.ALL_PASSED
//                results.any { it.passed } -> TestingStatus.SOME_FAILED
//                else -> TestingStatus.ERROR
//            }
//
//        return TestingResult(
//            snippetId = request.snippetId,
//            userId = request.userId,
//            status = status,
//            results = results,
//            executedAt = java.time.Instant.now().toString(),
//        )
//    }
//}
