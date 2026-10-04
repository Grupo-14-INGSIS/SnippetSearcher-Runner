package com.grupo14IngSis.snippetSearcherRunner.engine

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import org.springframework.stereotype.Component

@Component
class PrintScriptLanguageRunner(
    private val formattingPlugin: FormattingPlugin,
    private val analyzerPlugin: AnalyzerPlugin,
) : LanguageRunner {
    override val language: String = "printscript"
    override val fileExtension: String = "ps"

    override fun execute(code: String, inputs: List<String>, env: Map<String, String>): ExecutionOutput {
        return ExecutionOutput(
            status = "COMPLETED",
            outputs = emptyList(),
        )
    }

    override fun format(code: String, rules: Map<String, Any>): String {
        return (formattingPlugin.run(code, mapOf("rules" to rules)) as? String) ?: code
    }

    override fun lint(code: String, rules: Map<String, Any>): LintOutput {
        val result = (analyzerPlugin.run(code, mapOf("rules" to rules)) as? String) ?: ""
        val isCompliant = !result.contains("error", ignoreCase = true)
        return LintOutput(isCompliant = isCompliant, messages = listOf(result))
    }
}
