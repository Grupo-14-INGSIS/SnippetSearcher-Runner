package com.grupo14IngSis.snippetSearcherRunner.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type

@Entity
@Table(name = "formatting_rules")
@IdClass(FormattingRuleId::class)
data class FormattingRule(
  @Id
  @Column(name = "user_id")
  val userId: String = "",

  @Id
  @Column(name = "set_language")
  val setLanguage: String = "",

  @Type(JsonType::class)
  @Column(name = "config_rules", columnDefinition = "jsonb")
  var configRules: MutableMap<String, Any>

)