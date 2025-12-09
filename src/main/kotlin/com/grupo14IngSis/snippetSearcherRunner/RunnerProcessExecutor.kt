package com.grupo14IngSis.snippetSearcherRunner

import java.io.File

object RunnerProcessExecutor {
    fun run(
        path: String,
        version: String,
    ): String {
        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val classpath = System.getProperty("java.class.path")

        val processBuilder =
            ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                "com.grupo14IngSis.snippetSearcherRunner.RunnerWrapper",
                path,
                version,
            )

        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        return output
    }
}
