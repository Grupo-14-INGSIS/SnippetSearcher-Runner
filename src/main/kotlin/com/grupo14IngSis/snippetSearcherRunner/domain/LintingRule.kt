package com.grupo14IngSis.snippetSearcherRunner.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.PostLoad
import jakarta.persistence.Table
import jakarta.persistence.Transient

/**
 * Reglas de linteo de un usuario para un lenguaje. Cada regla es una fila de
 * `linting_rule_values`; [configRules] es la vista como mapa que usa el resto del código.
 * Después de modificar [configRules], llamar a [syncValues] antes de guardar.
 */
@Entity
@Table(name = "linting_rules")
@IdClass(LintingRuleId::class)
data class LintingRule(
    @Id
    @Column(name = "user_id")
    val userId: String = "",
    @Id
    @Column(name = "set_language")
    val setLanguage: String = "",
    @Transient
    var configRules: MutableMap<String, Any>? = null,
) {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "linting_rule_values",
        joinColumns = [
            JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            JoinColumn(name = "set_language", referencedColumnName = "set_language"),
        ],
    )
    @MapKeyColumn(name = "rule_name")
    var values: MutableMap<String, RuleValue> = RuleValue.fromMap(configRules)

    /** Copia [configRules] a las filas que se guardan. */
    fun syncValues() {
        values.clear()
        values.putAll(RuleValue.fromMap(configRules))
    }

    @PostLoad
    fun loadConfigRules() {
        configRules = RuleValue.toMap(values)
    }
}
