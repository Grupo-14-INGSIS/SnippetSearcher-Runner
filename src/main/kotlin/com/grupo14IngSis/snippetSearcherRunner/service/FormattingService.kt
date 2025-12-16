package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRule
import com.grupo14IngSis.snippetSearcherRunner.domain.FormattingRuleId
import com.grupo14IngSis.snippetSearcherRunner.repository.FormattingRulesRepository
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin // Import FormattingPlugin
import org.springframework.beans.factory.annotation.Qualifier // Import Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Files

@Service
class FormattingService(
    private val repository: FormattingRulesRepository,
    @Qualifier("formatter") private val formattingPlugin: FormattingPlugin, // Inject FormattingPlugin
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
        val params = mapOf(
            "version" to version,
            // Assuming default config path or dynamically fetched path
            // For now, we need to pass a configFile path to the plugin
            // The FormattingPlugin expects a 'configFile' parameter, which it expects to exist on the Runner's filesystem.
            // For now, I'll pass a dummy path that will be created by the plugin if not exists, or handle it as part of the plugin logic.
            "configFile" to createDefaultFormattingConfigFile().absolutePath // Create a default config file for the plugin
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
        tempDir.deleteOnExit() // Ensure the temporary directory is deleted on exit
        return configFile
    }
}
