package com.grupo14IngSis.snippetSearcherRunner.dto

data class LintingError(
    val message: String,
    val line: Int,
    val column: Int,
)
