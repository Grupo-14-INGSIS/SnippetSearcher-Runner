package com.grupo14IngSis.snippetSearcherRunner.dto

data class ErrorResponse(
    val status: Int,
    val message: String,
    val details: String,
)
