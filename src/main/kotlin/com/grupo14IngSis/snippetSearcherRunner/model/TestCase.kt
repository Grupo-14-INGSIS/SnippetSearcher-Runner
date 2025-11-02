package com.grupo14IngSis.snippetSearcherRunner.model

import java.time.LocalDateTime

data class TestCase(
    val id: String,
    val snippetId: Long,
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)