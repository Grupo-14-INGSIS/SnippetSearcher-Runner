package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service

@Service("test")
class TestPlugin : RunnerPlugin {
    override fun run(
        snippet: String?,
        params: Map<String, Any>?,
    ): Any {
        val safeSnippet = "received: \"$snippet\""
        println(
            "###########################################################\n" +
                "# TESTING TESTING TESTING TESTING TESTING TESTING TESTING #" +
                "###########################################################\n" +
                "#\n" +
                "# $safeSnippet\n" +
                "#\n" +
                "###########################################################\n" +
                "# TESTING TESTING TESTING TESTING TESTING TESTING TESTING #" +
                "###########################################################",
        )
        return safeSnippet
    }
}
