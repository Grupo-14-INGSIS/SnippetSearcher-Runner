package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service

@Service("formatting")
class FormattingPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        TODO("Not yet implemented")
    }
}
