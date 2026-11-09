package com.grupo14IngSis.snippetSearcherRunner.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * Valor de una regla, guardado como texto más su tipo para devolverlo igual que entró
 * (`true`, `2` o `"camel case"`). Reemplaza al JSONB `config_rules`: cada regla es una fila.
 */
@Embeddable
data class RuleValue(
    @Column(name = "rule_value", nullable = false, columnDefinition = "text")
    val value: String = "",
    @Column(name = "value_type", nullable = false, length = 16)
    val type: String = STRING,
) {
    fun toAny(): Any =
        when (type) {
            BOOLEAN -> value.toBoolean()
            NUMBER -> value.toIntOrNull() ?: value.toLongOrNull() ?: value.toDouble()
            else -> value
        }

    companion object {
        const val BOOLEAN = "boolean"
        const val NUMBER = "number"
        const val STRING = "string"

        fun of(value: Any): RuleValue =
            when (value) {
                is Boolean -> RuleValue(value.toString(), BOOLEAN)
                is Number -> RuleValue(value.toString(), NUMBER)
                else -> RuleValue(value.toString(), STRING)
            }

        fun fromMap(rules: Map<String, Any>?): MutableMap<String, RuleValue> =
            rules.orEmpty().mapValuesTo(mutableMapOf()) { (_, v) -> of(v) }

        fun toMap(values: Map<String, RuleValue>?): MutableMap<String, Any> =
            values.orEmpty().mapValuesTo(mutableMapOf()) { (_, v) -> v.toAny() }
    }
}
