package com.grupo14IngSis.snippetSearcherRunner.dto

data class ExecutionRequest(
    val userId: String,
    val environment: Map<String, String>,
    val version: String = "1.0",
)
