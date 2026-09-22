package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import runner.src.main.kotlin.Runner
import java.io.File

@Service("formatter")
class FormattingPlugin() : RunnerPlugin {
    private val runner = Runner()

    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        if (snippet.isNullOrBlank()) {
            return ""
        }

        val version = params?.get("version") as? String ?: "1.0"

        var tempConfigFile: File? = null
        val configFile: File
        val filteredParams = params?.filterKeys { it != "version" && it != "configFile" } ?: emptyMap()
        if (params?.get("configFile") is String) {
            val path = params["configFile"] as String
            configFile = File(path)
            if (!configFile.exists()) {
                throw IllegalArgumentException("Configuration file does not exist at path: $path")
            }
        } else if (params?.containsKey("rules") == true || filteredParams.isNotEmpty()) {
            tempConfigFile = File.createTempFile("formatting-config-", ".yaml")
            val yaml = Yaml()
            val rulesContent = (params?.get("rules") as? Map<*, *>) ?: filteredParams
            tempConfigFile.writeText(yaml.dump(rulesContent))
            configFile = tempConfigFile
        } else {
            throw IllegalArgumentException("Configuration file path 'configFile' is required for formatting.")
        }

        // 1. Create a temporary file for the snippet
        val tempFile = File.createTempFile("snippet-format-", ".ps")
        tempFile.writeText(snippet)

        try {
            // 2. Prepare arguments for the runner
            val args = mutableListOf(tempFile.absolutePath, configFile.absolutePath, version)

            // 3. Instantiate and run the command
            runner.formatterCommand(args) // This modifies tempFile in-place

            // 4. Read the formatted content back from the temp file
            return tempFile.readText()
        } finally {
            // 5. Clean up the temporary files
            tempFile.delete()
            tempConfigFile?.delete()
        }
    }
}
