package com.grupo14IngSis.snippetSearcherRunner.engine

import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class LanguageRunnerRegistry(
    runners: List<LanguageRunner>,
) {
    private val runnerMap = runners.associateBy { it.language.lowercase() }

    fun getRunner(language: String): LanguageRunner? = runnerMap[language.lowercase()]

    fun isSupported(language: String): Boolean = runnerMap.containsKey(language.lowercase())

    fun getSupportedLanguages(): Set<String> = runnerMap.keys

    fun require(language: String): LanguageRunner = getRunner(language) ?: throw IllegalArgumentException("Unsupported language: $language")
}
