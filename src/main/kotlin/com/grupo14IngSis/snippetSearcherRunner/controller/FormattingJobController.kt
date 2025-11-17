package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/formatting")
class FormattingJobController(
    private val formattingConfigService: FormattingConfigService,
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

}
