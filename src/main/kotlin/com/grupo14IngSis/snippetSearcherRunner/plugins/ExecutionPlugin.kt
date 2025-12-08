package com.grupo14IngSis.snippetSearcherRunner.plugins

import com.grupo14IngSis.snippetSearcherRunner.RunnerProcessExecutor
import org.springframework.stereotype.Service

@Service("executor")
class ExecutionPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        if (snippet.isNullOrBlank()) return ""
        val version = params?.get("version")?.toString() ?: "1.0"
        val tempFile = createTempFile(suffix = ".ps")
        try {
            tempFile.writeText(snippet)

            val output = RunnerProcessExecutor.run(tempFile.absolutePath, version)
            return output.ifBlank { "Execution produced no output" }
        } finally {
            tempFile.delete()
        }
    }
}
