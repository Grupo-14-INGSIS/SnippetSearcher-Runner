package com.grupo14IngSis.snippetSearcherRunner.testing.dto

data class StartTestResponse(
    val jobId:  String,
    val snippetId: String,
    val input: String,
    val expected: String,
)
