package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/formatting")
class FormattingConfigController(
    private val formattingConfigService: FormattingConfigService
) {

    /**
     * Obtiene todas las reglas de formateo disponibles
     *
     * GET /api/v1/formatting/rules
     */
    @GetMapping("/rules")
    fun getAvailableRules(): ResponseEntity<List<FormattingRuleDto>> {
        val rules = formattingConfigService.getAvailableRules()
        return ResponseEntity.ok(rules)
    }

    /**
     * Obtiene la configuración de formateo de un usuario
     *
     * GET /api/v1/formatting/config?userId=user123
     */
    @GetMapping("/config")
    fun getUserConfig(
        @RequestParam userId: String
    ): ResponseEntity<FormattingConfigDto> {
        val config = formattingConfigService.getUserConfig(userId)
        return ResponseEntity.ok(config)
    }

    /**
     * Habilita o deshabilita una regla específica
     *
     * PUT /api/v1/formatting/config/rule?userId=user123
     * Body: { "ruleId": "space-before-colon", "enabled": true }
     */
    @PutMapping("/config/rule")
    fun updateRule(
        @RequestParam userId: String,
        @RequestBody request: UpdateRuleRequest
    ): ResponseEntity<FormattingConfigResponse> {
        val response = formattingConfigService.updateRule(
            userId,
            request.ruleId,
            request.enabled
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Actualiza múltiples reglas a la vez
     *
     * PUT /api/v1/formatting/config/bulk?userId=user123
     * Body: {
     *   "rules": {
     *     "space-before-colon": true,
     *     "space-after-colon": false,
     *     "indentation": true
     *   }
     * }
     */
    @PutMapping("/config/bulk")
    fun bulkUpdateRules(
        @RequestParam userId: String,
        @RequestBody request: BulkUpdateRulesRequest
    ): ResponseEntity<FormattingConfigResponse> {
        val response = formattingConfigService.bulkUpdateRules(
            userId,
            request.rules
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Restablece la configuración a los valores por defecto
     *
     * POST /api/v1/formatting/config/reset?userId=user123
     */
    @PostMapping("/config/reset")
    fun resetToDefaults(
        @RequestParam userId: String
    ): ResponseEntity<FormattingConfigResponse> {
        val response = formattingConfigService.resetToDefaults(userId)
        return ResponseEntity.ok(response)
    }

    /**
     * Obtiene solo las reglas habilitadas de un usuario
     *
     * GET /api/v1/formatting/config/enabled?userId=user123
     */
    @GetMapping("/config/enabled")
    fun getEnabledRules(
        @RequestParam userId: String
    ): ResponseEntity<List<FormattingRuleDto>> {
        val enabledRules = formattingConfigService.getEnabledRules(userId)
            .map { rule ->
                FormattingRuleDto(
                    id = rule.id,
                    name = rule.name,
                    description = rule.description,
                    enabled = rule.enabled,
                    category = rule.category.name
                )
            }
        return ResponseEntity.ok(enabledRules)
    }

    /**
     * Verifica si una regla específica está habilitada
     *
     * GET /api/v1/formatting/config/enabled/{ruleId}?userId=user123
     */
    @GetMapping("/config/enabled/{ruleId}")
    fun isRuleEnabled(
        @PathVariable ruleId: String,
        @RequestParam userId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val enabled = formattingConfigService.isRuleEnabled(userId, ruleId)
        return ResponseEntity.ok(mapOf("enabled" to enabled))
    }
}