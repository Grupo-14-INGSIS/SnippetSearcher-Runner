package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner // Assuming this is the package from the imported runner
import org.springframework.stereotype.Service
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

        val configPath =
            params?.get("configFile") as? String
                ?: throw IllegalArgumentException("Configuration file path 'configFile' is required for formatting.")

        val version = params["version"] as? String

        val configFile = File(configPath)
        if (!configFile.exists()) {
            throw IllegalArgumentException("Configuration file does not exist at path: $configPath")
        }

        // 1. Create a temporary file for the snippet
        val tempFile = createTempFile(suffix = ".ps")
        tempFile.writeText(snippet)

        try {
            // 2. Prepare arguments for the runner
            val args = mutableListOf(tempFile.absolutePath, configFile.absolutePath)
            if (version != null) {
                args.add(version)
            }

            // 3. Instantiate and run the command
            runner.formatterCommand(args) // This modifies tempFile in-place

            // 4. Read the formatted content back from the temp file
            return tempFile.readText()
        } finally {
            // 5. Clean up the temporary file
            tempFile.delete()
        }
    }
}
