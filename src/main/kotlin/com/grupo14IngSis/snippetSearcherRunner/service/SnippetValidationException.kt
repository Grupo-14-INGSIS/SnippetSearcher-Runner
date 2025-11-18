package com.grupo14IngSis.snippetSearcherRunner.service

class SnippetValidationException(
    override val message: String,
    val rule: String?,
    val line: Int?,
    val column: Int?,
) : RuntimeException(message)
