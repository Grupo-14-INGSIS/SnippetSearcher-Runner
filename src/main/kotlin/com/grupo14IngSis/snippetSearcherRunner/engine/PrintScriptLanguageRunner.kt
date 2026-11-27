package com.grupo14IngSis.snippetSearcherRunner.engine

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintReport
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import org.springframework.stereotype.Component

@Component
class PrintScriptLanguageRunner(
    private val validationPlugin: ValidationPlugin,
    private val formattingPlugin: FormattingPlugin,
    private val analyzerPlugin: AnalyzerPlugin,
) : LanguageRunner {
    override val language: String = "printscript"
    override val fileExtension: String = "ps"

    override fun validate(
        code: String,
        version: String,
        env: Map<String, String>,
    ): String? {
        val result = validationPlugin.run(code, mapOf("version" to version)) as String
        val hasError = VALIDATION_ERRORS.any { result.contains(it, ignoreCase = true) }
        return if (hasError) result else null
    }

    override fun format(
        code: String,
        rules: Map<String, Any>,
        version: String,
    ): String = formattingPlugin.run(code, rules + ("version" to version)) as String

    override fun lint(
        code: String,
        rules: Map<String, Any>,
        version: String,
    ): LintReport = analyzerPlugin.lint(code, rules + ("version" to version))

    companion object {
        private val VALIDATION_ERRORS = listOf("ERROR", "Exception", "Syntax error", "Parsing error")
    }
}
