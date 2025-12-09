package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.springframework.stereotype.Service

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

        val configFileContent =
            params?.get("configFileContent") as? String
                ?: throw IllegalArgumentException("Configuration file content 'configFileContent' is required for analysis.")

        val version = params["version"] as? String

        val tempSnippetFile = createTempFile(suffix = ".ps")
        tempSnippetFile.writeText(snippet)

        val tempConfigFile = createTempFile(suffix = ".yaml")
        tempConfigFile.writeText(configFileContent)

        try {
            val args = mutableListOf(tempSnippetFile.absolutePath, tempConfigFile.absolutePath)
            if (version != null) {
                args.add(version)
            }
            runner.analyzerCommand(args)
            return tempSnippetFile.readText()
        } finally {
            tempSnippetFile.delete()
            tempConfigFile.delete()
        }
    }
}
