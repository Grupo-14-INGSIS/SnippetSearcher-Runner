package com.grupo14IngSis.snippetSearcherRunner.engine

import org.springframework.stereotype.Service

@Service
class LanguageRunnerRegistry(runners: List<LanguageRunner>) {
    private val runnerMap = runners.associateBy { it.language.lowercase() }

    fun getRunner(language: String): LanguageRunner? {
        return runnerMap[language.lowercase()]
    }

    fun isSupported(language: String): Boolean {
        return runnerMap.containsKey(language.lowercase())
    }

    fun getSupportedLanguages(): Set<String> {
        return runnerMap.keys
    }
}
