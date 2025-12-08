package com.grupo14IngSis.snippetSearcherRunner.dto

data class ExecutionEvent(
    val type: ExecutionEventType,
    val message: String? = null,
)
