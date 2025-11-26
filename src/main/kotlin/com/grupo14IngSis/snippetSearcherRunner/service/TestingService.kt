package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

  @Service
  class TestingService {

    private val testPlugin = TestPlugin()

    fun testSnippet(snippet: String, params: List<String>, expected: String): TestResponse {
      val actual = testPlugin.run(snippet, params.associateWith { it }) as String
      val result = if (actual == expected) TestResult.PASSED else TestResult.FAILED
      return TestResponse(actual, result)
    }

  }