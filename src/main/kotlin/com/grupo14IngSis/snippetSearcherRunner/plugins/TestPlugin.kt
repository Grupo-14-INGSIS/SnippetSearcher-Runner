package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service

@Service("test")
class TestPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        var safeSnippet: String
        if (snippet == null) {
            safeSnippet = "snippet"
        } else {
            safeSnippet = snippet
        }
        var safeParams: List<String>
        if (params == null) {
            safeParams = listOf("params")
        } else {
            safeParams = params.entries.toList() as List<*> as List<String>
        }
        return "$safeSnippet $safeParams"
    }
}
