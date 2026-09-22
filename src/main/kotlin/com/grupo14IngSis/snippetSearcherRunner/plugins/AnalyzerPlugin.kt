package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import runner.src.main.kotlin.Runner
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

@Service("analyzer")
class AnalyzerPlugin() : RunnerPlugin {
    private val runner = Runner()

    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        if (snippet.isNullOrBlank()) {
            return ""
        }

        val version = params?.get("version") as? String ?: "1.0"

        val tempSnippetFile = File.createTempFile("snippet-lint-", ".ps")
        tempSnippetFile.writeText(snippet)

        var tempConfigFile: File? = null
        val configFile: File

        val rulesMap = (params?.get("rules") as? Map<*, *>)
        val filteredParams = params?.filterKeys { it != "version" && it != "configFile" && it != "configFileContent" } ?: emptyMap()

        if (params?.get("configFile") is String) {
            val path = params["configFile"] as String
            configFile = File(path)
            if (!configFile.exists()) {
                throw IllegalArgumentException("Configuration file does not exist at path: $path")
            }
        } else if (params?.get("configFileContent") is String) {
            tempConfigFile = File.createTempFile("lint-config-", ".yaml")
            tempConfigFile.writeText(params["configFileContent"] as String)
            configFile = tempConfigFile
        } else if (rulesMap != null || filteredParams.isNotEmpty()) {
            // Build rules configuration structure expected by LintRuleRegistry
            val lintRulesMap = mutableMapOf<String, Any>()
            val rulesSource = rulesMap ?: filteredParams
            for ((k, v) in rulesSource) {
                val key = k.toString()
                if (key == "version" || key == "configFile" || key == "configFileContent") continue
                if (key == "identifier_format") {
                    val style = if (v is String && v.isNotBlank()) v else if (v == true) "camelCase" else "none"
                    lintRulesMap[key] = mapOf("style" to style)
                } else if (key.contains("println")) {
                    val enabled = if (v is Boolean) v else true
                    lintRulesMap["mandatory-variable-or-literal-in-println"] = mapOf("enabled" to enabled)
                } else if (key.contains("readInput")) {
                    val enabled = if (v is Boolean) v else true
                    lintRulesMap["mandatory-variable-or-literal-in-readInput"] = mapOf("enabled" to enabled)
                } else if (key.contains("if-without-else") || key.contains("if_without_else")) {
                    val enabled = if (v is Boolean) v else true
                    lintRulesMap["if-without-else"] = mapOf("enabled" to enabled)
                } else if (v is Map<*, *>) {
                    lintRulesMap[key] = v
                } else if (v is Boolean) {
                    lintRulesMap[key] = mapOf("enabled" to v)
                }
            }

            tempConfigFile = File.createTempFile("lint-config-", ".yaml")
            val yaml = Yaml()
            tempConfigFile.writeText(yaml.dump(mapOf("rules" to lintRulesMap)))
            configFile = tempConfigFile
        } else {
            throw IllegalArgumentException("Configuration file content 'configFileContent' is required for analysis.")
        }

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        try {
            val args = mutableListOf(tempSnippetFile.absolutePath, configFile.absolutePath, version)
            runner.analyzerCommand(args)
            System.out.flush()
            val output = outputStream.toString()
            return if (output.isBlank() || output.contains("No issues were found") || output.contains("SUCCESS")) snippet else output
        } finally {
            System.setOut(originalOut)
            tempSnippetFile.delete()
            tempConfigFile?.delete()
        }
    }
}
