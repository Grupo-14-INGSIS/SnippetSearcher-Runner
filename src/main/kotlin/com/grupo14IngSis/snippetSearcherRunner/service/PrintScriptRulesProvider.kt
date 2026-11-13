package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.LanguageRuleEntry

class PrintScriptRulesProvider : RulesProvider {
    private val formattingRules =
        mapOf(
            "enforce-spacing-around-equals" to true,
            "enforce-no-spacing-around-equals" to false,
            "enforce-spacing-before-colon-in-declaration" to false,
            "enforce-spacing-after-colon-in-declaration" to true,
            // Estas tres son un número en la librería (líneas en blanco / espacios de sangría): con un booleano
            // la librería las toma como 0 y 2 y la UI no muestra dónde cambiarlas. Mismos valores, pero editables.
            "line-breaks-after-println" to 0,
            "line-breaks-before-println" to 0,
            "indent-inside-if" to 2,
            "if-brace-below-line" to true,
            "mandatory-single-space-separation" to true,
            "mandatory-space-surrounding-operations" to true,
            "mandatory-line-break-after-statement" to true,
        )

    private val lintingRules =
        mapOf(
            "identifier_format" to true,
            "mandatory-variable-or-literal-in-println" to true,
            "mandatory-variable-or-literal-in-readInput" to true,
        )

    override fun getFormattingRules(version: String?): LanguageRuleEntry = LanguageRuleEntry("printscript", formattingRules)

    override fun getLintingRules(version: String?): LanguageRuleEntry = LanguageRuleEntry("printscript", lintingRules)
}
