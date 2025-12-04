package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@Service("executor")
class ExecutionPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        if (snippet.isNullOrBlank()) {
            return ""
        }

        val version = params?.get("version") as? String ?: "1.0"

        val tempFile = createTempFile(suffix = ".ps")
        tempFile.writeText(snippet)

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        try {
            val runner = Runner()
            runner.executionCommand(listOf(tempFile.absolutePath, version))
        } finally {
            System.setOut(originalOut)
            tempFile.delete()
        }

        return outputStream.toString()
    }
}
