package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.model.DefaultPrintScriptRules
import com.grupo14IngSis.snippetSearcherRunner.model.FormattingConfig
import com.grupo14IngSis.snippetSearcherRunner.model.FormattingRule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class FormattingConfigService {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Almacenamiento en memoria de configuraciones por usuario
    private val userConfigs = ConcurrentHashMap<String, FormattingConfig>()

    /**
     * Obtiene todas las reglas disponibles
     */
    fun getAvailableRules(): List<FormattingRuleDto> {
        logger.debug("Fetching all available formatting rules")

        return DefaultPrintScriptRules.ALL_RULES.map { it.toDto() }
    }

    /**
     * Obtiene la configuración de reglas de un usuario
     * Si no existe, crea una con los valores por defecto
     */
    fun getUserConfig(userId: String): FormattingConfigDto {
        logger.debug("Fetching formatting config for user: $userId")

        val config =
            userConfigs.getOrPut(userId) {
                logger.info("Creating default formatting config for user: $userId")
                FormattingConfig(
                    userId = userId,
                    rules = DefaultPrintScriptRules.getDefaultConfig(),
                )
            }

        return config.toDto()
    }

    /**
     * Habilita o deshabilita una regla específica
     */
    fun updateRule(
        userId: String,
        ruleId: String,
        enabled: Boolean,
    ): FormattingConfigResponse {
        logger.info("Updating rule '$ruleId' to $enabled for user: $userId")

        // Verificar que la regla existe
        val ruleExists = DefaultPrintScriptRules.ALL_RULES.any { it.id == ruleId }
        if (!ruleExists) {
            logger.warn("Rule not found: $ruleId")
            return FormattingConfigResponse(
                success = false,
                message = "Rule not found: $ruleId",
                config = null,
            )
        }

        // Obtener o crear configuración del usuario
        val config =
            userConfigs.getOrPut(userId) {
                FormattingConfig(
                    userId = userId,
                    rules = DefaultPrintScriptRules.getDefaultConfig(),
                )
            }

        // Actualizar la regla
        val updatedRules = config.rules.toMutableMap()
        updatedRules[ruleId] = enabled

        val updatedConfig =
            config.copy(
                rules = updatedRules,
                lastUpdated = LocalDateTime.now().toString(),
            )

        userConfigs[userId] = updatedConfig

        logger.info("Rule '$ruleId' updated successfully for user: $userId")

        return FormattingConfigResponse(
            success = true,
            message = "Rule updated successfully",
            config = updatedConfig.toDto(),
        )
    }

    /**
     * Actualiza múltiples reglas a la vez
     */
    fun bulkUpdateRules(
        userId: String,
        rulesUpdate: Map<String, Boolean>,
    ): FormattingConfigResponse {
        logger.info("Bulk updating ${rulesUpdate.size} rules for user: $userId")

        // Verificar que todas las reglas existen
        val validRuleIds = DefaultPrintScriptRules.ALL_RULES.map { it.id }.toSet()
        val invalidRules = rulesUpdate.keys.filter { it !in validRuleIds }

        if (invalidRules.isNotEmpty()) {
            logger.warn("Invalid rules found: $invalidRules")
            return FormattingConfigResponse(
                success = false,
                message = "Invalid rules: ${invalidRules.joinToString(", ")}",
                config = null,
            )
        }

        // Obtener o crear configuración del usuario
        val config =
            userConfigs.getOrPut(userId) {
                FormattingConfig(
                    userId = userId,
                    rules = DefaultPrintScriptRules.getDefaultConfig(),
                )
            }

        // Actualizar todas las reglas
        val updatedRules = config.rules.toMutableMap()
        updatedRules.putAll(rulesUpdate)

        val updatedConfig =
            config.copy(
                rules = updatedRules,
                lastUpdated = LocalDateTime.now().toString(),
            )

        userConfigs[userId] = updatedConfig

        logger.info("Bulk update completed for user: $userId")

        return FormattingConfigResponse(
            success = true,
            message = "Rules updated successfully",
            config = updatedConfig.toDto(),
        )
    }

    /**
     * Restablece la configuración a los valores por defecto
     */
    fun resetToDefaults(userId: String): FormattingConfigResponse {
        logger.info("Resetting formatting config to defaults for user: $userId")

        val defaultConfig =
            FormattingConfig(
                userId = userId,
                rules = DefaultPrintScriptRules.getDefaultConfig(),
                lastUpdated = LocalDateTime.now().toString(),
            )

        userConfigs[userId] = defaultConfig

        logger.info("Config reset to defaults for user: $userId")

        return FormattingConfigResponse(
            success = true,
            message = "Configuration reset to defaults",
            config = defaultConfig.toDto(),
        )
    }

    /**
     * Obtiene solo las reglas habilitadas para un usuario
     */
    fun getEnabledRules(userId: String): List<FormattingRule> {
        val config =
            userConfigs.getOrPut(userId) {
                FormattingConfig(
                    userId = userId,
                    rules = DefaultPrintScriptRules.getDefaultConfig(),
                )
            }

        return DefaultPrintScriptRules.ALL_RULES.filter { rule ->
            config.rules[rule.id] == true
        }
    }

    /**
     * Verifica si una regla específica está habilitada
     */
    fun isRuleEnabled(
        userId: String,
        ruleId: String,
    ): Boolean {
        val config =
            userConfigs.getOrPut(userId) {
                FormattingConfig(
                    userId = userId,
                    rules = DefaultPrintScriptRules.getDefaultConfig(),
                )
            }

        return config.rules[ruleId] ?: false
    }

    // Extension functions para conversión
    private fun FormattingRule.toDto() =
        FormattingRuleDto(
            id = id,
            name = name,
            description = description,
            enabled = enabled,
            category = category.name,
        )

    private fun FormattingConfig.toDto(): FormattingConfigDto {
        val rulesWithStatus =
            DefaultPrintScriptRules.ALL_RULES.map { rule ->
                FormattingRuleDto(
                    id = rule.id,
                    name = rule.name,
                    description = rule.description,
                    enabled = this.rules[rule.id] ?: rule.enabled,
                    category = rule.category.name,
                )
            }

        return FormattingConfigDto(
            userId = userId,
            rules = rulesWithStatus,
            lastUpdated = lastUpdated,
        )
    }
}
