package com.grupo14IngSis.snippetSearcherRunner.engine

data class ExecutionOutput(
    val status: String,
    val outputs: List<String>,
    val error: String? = null,
)

data class LintOutput(
    val isCompliant: Boolean,
    val messages: List<String> = emptyList(),
)

interface LanguageRunner {
    val language: String
    val fileExtension: String

    fun execute(
        code: String,
        inputs: List<String>,
        env: Map<String, String> = emptyMap(),
    ): ExecutionOutput

    fun format(
        code: String,
        rules: Map<String, Any> = emptyMap(),
    ): String

    fun lint(
        code: String,
        rules: Map<String, Any> = emptyMap(),
    ): LintOutput
}
