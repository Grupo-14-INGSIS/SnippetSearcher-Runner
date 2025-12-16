package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Files

@Service
class FormattingService(
    private val repository: FormattingRulesRepository,
    @Qualifier("formatter") private val formattingPlugin: FormattingPlugin,
) {
    fun getRules(
        userId: String,
        language: String,
    ): Map<String, Any> {
        val id = FormattingRuleId(userId, language)
        val rules: FormattingRule = repository.findById(id).orElse(null) ?: return mapOf()
        return rules.configRules!!
    }

    @Transactional
    fun updateRules(
        userId: String,
        language: String,
        newRules: Map<String, Any>,
    ) {
        val id = FormattingRuleId(userId, language)
        val existing: FormattingRule =
            repository.findById(id).orElseThrow {
                IllegalArgumentException("User not found or language not found")
            }
        existing.configRules?.putAll(newRules)
        repository.save(existing)
    }

    fun formatSnippet(
        content: String,
        version: String,
    ): String {
        // Use the plugin directly
        val params =
            mapOf(
                "version" to version,
                // Create a default config file for the plugin
                "configFile" to createDefaultFormattingConfigFile().absolutePath,
            )
        return formattingPlugin.run(content, params) as String
    }

    private fun createDefaultFormattingConfigFile(): File {
        val tempDir = Files.createTempDirectory("printscript-format-config").toFile()
        val configFile = File(tempDir, "format-config.yaml")
        configFile.writeText(
            """
            rules:
              semicolon:
                active: true
              indentation:
                active: true
                size: 4
              space_after_colon:
                active: true
              space_after_commas:
                active: true
              space_around_operators:
                active: true
              empty_lines:
                active: true
                max: 2
            """.trimIndent(),
        )
        // Ensure the temporary directory is deleted on exit
        tempDir.deleteOnExit()
        return configFile
    }
}
