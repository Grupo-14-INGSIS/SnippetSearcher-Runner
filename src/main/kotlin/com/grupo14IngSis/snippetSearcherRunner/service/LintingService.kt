package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.LintingRuleId
import com.grupo14IngSis.snippetSearcherRunner.dto.LintingError
import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.RunnerPlugin
import com.grupo14IngSis.snippetSearcherRunner.repository.LintingRulesRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Files

@Service
class LintingService(
    private val repository: LintingRulesRepository,
    // Inject LintingPlugin
    @Qualifier("linter") private val lintingPlugin: AnalyzerPlugin,
) {
    fun getRules(
        userId: String,
        language: String,
    ): Map<String, Any> {
        val id = LintingRuleId(userId, language)
        val rules: LintingRule = repository.findById(id).orElse(null) ?: return mapOf()
        return rules.configRules!!
    }

    @Transactional
    fun updateRules(
        userId: String,
        language: String,
        newRules: Map<String, Any>,
    ) {
        val id = LintingRuleId(userId, language)
        val existing: LintingRule =
            repository.findById(id).orElseThrow {
                IllegalArgumentException("User not found or language not found")
            }
        existing.configRules?.putAll(newRules)
        repository.save(existing)
    }

    fun lintSnippet(
        content: String,
        version: String,
    ): List<LintingError> {
        // Use the plugin directly
        val params =
            mapOf(
                "version" to version,
                // Create a default config file for the plugin
                "configFile" to createDefaultLintingConfigFile().absolutePath,
            )
        // Explicitly cast to RunnerPlugin to resolve ambiguity with Kotlin's run extension function
        val output = (lintingPlugin as RunnerPlugin).run(content, params) as String // Plugin returns String output

        return parseAnalyzerOutput(output)
    }

    private fun createDefaultLintingConfigFile(): File {
        val tempDir = Files.createTempDirectory("printscript-lint-config").toFile()
        val configFile = File(tempDir, "lint-config.yaml")
        configFile.writeText(
            """
            rules:
              semicolon_at_end:
                active: true
              no_var_keywords:
                active: true
              no_else_without_curly_braces:
                active: true
              if_else_rules:
                active: true
              max_line_length:
                active: true
                length: 80
            """,
        )
        // Ensure the temporary directory is deleted on exit
        tempDir.deleteOnExit()
        return configFile
    }

    private fun parseAnalyzerOutput(output: String): List<LintingError> {
        val errors = mutableListOf<LintingError>()
        // Regex to parse lines like: "  - [LinterError] message at (line:X, column:Y)"
        val errorRegex = "\\[LinterError\\] (.+) at \\(line:(\\d+), column:(\\d+)\\)".toRegex()

        output.lineSequence().forEach { line ->
            val match = errorRegex.find(line)
            if (match != null) {
                val (message, lineNumStr, colNumStr) = match.destructured
                errors.add(LintingError(message, lineNumStr.toInt(), colNumStr.toInt()))
            }
        }
        return errors
    }
}
