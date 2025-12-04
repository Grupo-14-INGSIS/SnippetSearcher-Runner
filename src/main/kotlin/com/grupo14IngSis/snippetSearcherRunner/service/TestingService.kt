package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import org.springframework.stereotype.Service

@Service
class TestingService {
    private val testPlugin = TestPlugin()

    fun testSnippet(
        snippet: String,
        params: List<String>,
        expected: String,
    ): TestResponse {
        val actual = testPlugin.run(snippet, params.associateWith { it }) as String
        val result = if (actual == expected) TestResult.PASSED else TestResult.FAILED
        return TestResponse(actual, result)
    }
}
