package com.grupo14IngSis.snippetSearcherRunner.engine

import com.grupo14IngSis.snippetSearcherRunner.plugins.LintReport

interface LanguageRunner {
    val language: String
    val fileExtension: String

    fun validate(
        code: String,
        version: String,
        env: Map<String, String> = emptyMap(),
    ): String?

    fun format(
        code: String,
        rules: Map<String, Any>,
        version: String,
    ): String

    fun lint(
        code: String,
        rules: Map<String, Any>,
        version: String,
    ): LintReport
}
