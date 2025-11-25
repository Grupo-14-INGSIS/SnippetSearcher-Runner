package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users/{userId}/linting/rules")
class LintingJobController(
    private val lintingService: LintingService
) {
    /**
     * GET    /api/v1/users/{userId}/linting/rules/{language}
     *
     * Get rule configuration for a user
     *
     * Response:
     *
     *    {
     *        rule1: conf1,
     *        rule2: conf2,
     *        ...
     *    }
     *
     * Error Responses:
     *
     *    {
     *        status: 404,
     *        message: "User not found"
     *    }
     */
    @GetMapping("/{language}")
    fun getRules(
        @PathVariable userId: String,
        @PathVariable language: String,
    ): ResponseEntity<Map<String, Any>> {
        val rules: Map<String, Any> = lintingService.getRules(userId, language)
        if (rules.isEmpty()) {
            return ResponseEntity.status(404).body(mapOf("message" to "User or language not found"))
        }
        return ResponseEntity.ok().body(rules)
    }

    /**
     * PATCH   /api/v1/users/{userId}/linting/rules/{language}
     *
     * Add or modify a configuration rule for a user
     *
     * Request:
     *
     *    {
     *        rule1: newVal,
     *        rule2: newVal,
     *        rule4: newVal
     *    }
     *
     * Successful Response:
     *
     *    (empty body)
     *
     * Error Responses:
     *
     *    {
     *        status: 400,
     *        message: "User not found" / "Language not found"
     *    }
     */
    @PatchMapping("/{language}")
    fun editRules(
        @PathVariable userId: String,
        @PathVariable language: String,
        @RequestBody request: Map<String, Any>,
    ): ResponseEntity<*> {
        try {
            lintingService.updateRules(userId, language, request)
            return ResponseEntity.noContent().build<String>()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(404).body(mapOf("message" to e.message))
        }
    }
}