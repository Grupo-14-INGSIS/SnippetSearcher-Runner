package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@Service("analyzer")
class AnalyzerPlugin : RunnerPlugin {
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

        val version = params?.get("version") as? String

        val tempSnippetFile = createTempFile(suffix = ".ps")
        tempSnippetFile.writeText(snippet)

        val tempConfigFile = createTempFile(suffix = ".yaml")
        tempConfigFile.writeText(configFileContent)

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        try {
            val runner = Runner()
            val args = mutableListOf(tempSnippetFile.absolutePath, tempConfigFile.absolutePath)
            if (version != null) {
                args.add(version)
            }
            runner.analyzerCommand(args)
        } finally {
            System.setOut(originalOut)
            tempSnippetFile.delete()
            tempConfigFile.delete()
        }

        return outputStream.toString()
    }
}
