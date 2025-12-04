package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.example.Runner
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@Service("validation")
class ValidationPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        if (snippet.isNullOrBlank()) {
            return "No errors found."
        }

        val version = params?.get("version") as? String

        val tempFile = createTempFile(suffix = ".ps")
        tempFile.writeText(snippet)

        // Redirect stdout to capture the linter's output
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        System.setOut(printStream)

        try {
            val args = mutableListOf(tempFile.absolutePath)
            if (version != null) {
                args.add(version)
            }

            val runner = Runner()
            runner.validationCommand(args)

            System.out.flush()
            val validationOutput = outputStream.toString()

            return if (validationOutput.isBlank()) "No errors found." else validationOutput
        } finally {
            // Restore stdout and clean up
            System.setOut(originalOut)
            tempFile.delete()
        }
    }
}
