package com.grupo14IngSis.snippetSearcherRunner.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_rules", columnDefinition = "jsonb")
    var configRules: MutableMap<String, Any>? = null,
)
