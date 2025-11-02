package com.grupo14IngSis.snippetSearcherRunner.model

/**
 * Representa una regla de formateo de PrintScript
 */
data class FormattingRule(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val category: RuleCategory = RuleCategory.STYLE
)

enum class RuleCategory {
    STYLE,          // Reglas de estilo (espacios, indentación)
    NAMING,         // Reglas de nombres
    STRUCTURE,      // Reglas de estructura
    FORMATTING      // Reglas generales de formateo
}

/**
 * Configuración de reglas de formateo para un usuario
 */
data class FormattingConfig(
    val userId: String,
    val rules: Map<String, Boolean>, // ruleId -> enabled
    val lastUpdated: String = java.time.LocalDateTime.now().toString()
)

/**
 * Reglas predeterminadas de PrintScript
 */
object DefaultPrintScriptRules {

    val SPACE_BEFORE_COLON = FormattingRule(
        id = "space-before-colon",
        name = "Space Before Colon",
        description = "Enforces space before colon in type declarations",
        enabled = true,
        category = RuleCategory.STYLE
    )

    val SPACE_AFTER_COLON = FormattingRule(
        id = "space-after-colon",
        name = "Space After Colon",
        description = "Enforces space after colon in type declarations",
        enabled = true,
        category = RuleCategory.STYLE
    )

    val SPACE_AROUND_EQUALS = FormattingRule(
        id = "space-around-equals",
        name = "Space Around Equals",
        description = "Enforces spaces around assignment operators",
        enabled = true,
        category = RuleCategory.STYLE
    )

    val INDENTATION = FormattingRule(
        id = "indentation",
        name = "Indentation",
        description = "Enforces consistent indentation (4 spaces)",
        enabled = true,
        category = RuleCategory.STRUCTURE
    )

    val LINE_BREAKS_AFTER_SEMICOLON = FormattingRule(
        id = "line-breaks-after-semicolon",
        name = "Line Breaks After Semicolon",
        description = "Enforces line break after semicolons",
        enabled = true,
        category = RuleCategory.STRUCTURE
    )

    val NO_MULTIPLE_EMPTY_LINES = FormattingRule(
        id = "no-multiple-empty-lines",
        name = "No Multiple Empty Lines",
        description = "Prevents multiple consecutive empty lines",
        enabled = true,
        category = RuleCategory.STYLE
    )

    val CAMEL_CASE_VARIABLES = FormattingRule(
        id = "camel-case-variables",
        name = "CamelCase Variables",
        description = "Enforces camelCase naming for variables",
        enabled = true,
        category = RuleCategory.NAMING
    )

    val SNAKE_CASE_VARIABLES = FormattingRule(
        id = "snake-case-variables",
        name = "Snake_Case Variables",
        description = "Enforces snake_case naming for variables",
        enabled = false, // Deshabilitado por defecto (conflicto con camelCase)
        category = RuleCategory.NAMING
    )

    val MAX_LINE_LENGTH = FormattingRule(
        id = "max-line-length",
        name = "Max Line Length",
        description = "Enforces maximum line length (120 characters)",
        enabled = true,
        category = RuleCategory.STYLE
    )

    val PRINTLN_NEWLINE = FormattingRule(
        id = "println-newline",
        name = "Println New Line",
        description = "Enforces that println calls are on their own line",
        enabled = true,
        category = RuleCategory.FORMATTING
    )

    /**
     * Todas las reglas predeterminadas
     */
    val ALL_RULES = listOf(
        SPACE_BEFORE_COLON,
        SPACE_AFTER_COLON,
        SPACE_AROUND_EQUALS,
        INDENTATION,
        LINE_BREAKS_AFTER_SEMICOLON,
        NO_MULTIPLE_EMPTY_LINES,
        CAMEL_CASE_VARIABLES,
        SNAKE_CASE_VARIABLES,
        MAX_LINE_LENGTH,
        PRINTLN_NEWLINE
    )

    /**
     * Configuración por defecto (todas las reglas con su estado inicial)
     */
    fun getDefaultConfig(): Map<String, Boolean> {
        return ALL_RULES.associate { it.id to it.enabled }
    }
}