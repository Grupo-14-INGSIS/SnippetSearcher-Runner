package com.grupo14IngSis.snippetSearcherRunner.dto

data class ExecutionResponse(
    val status: ExecutionEventType,
    val message: String,
)
