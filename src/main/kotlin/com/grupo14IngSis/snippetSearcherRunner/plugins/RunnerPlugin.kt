package com.grupo14IngSis.snippetSearcherRunner.plugins

interface RunnerPlugin {
    fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any
}
