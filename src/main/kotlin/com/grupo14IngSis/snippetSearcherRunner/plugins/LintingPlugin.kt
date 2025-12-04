package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service

@Service("linting")
class LintingPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        TODO("Not yet implemented")
    }
}
