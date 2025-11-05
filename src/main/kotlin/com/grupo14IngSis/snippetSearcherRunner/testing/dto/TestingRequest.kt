package com.grupo14lngSis.snippetSearcherRunner.testing.dto

import com.grupo14IngSis.snippetSearcherRunner.testing.dto.TestCase

data class TestingRequest(
    val snippetId: String,
    val userId: String,
    val content: String,
    val language: String,
    val testCases: List<TestCase>,
)
