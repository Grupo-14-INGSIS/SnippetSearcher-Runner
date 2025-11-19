package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.*
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
    fun getAllRules(): ResponseEntity<List<FormattingRuleDto>> {
        val rules = formattingConfigService.getAvailableRules()
        return ResponseEntity.ok(rules)
    }

    @GetMapping("/rules/configurable")
    fun getConfigRules(): ResponseEntity<List<FormattingRuleDto>> {
        val rules = formattingConfigService.getAvailableRules()
        return ResponseEntity.ok(rules)
    }

    @GetMapping("/rules/mandatory")
    fun getMandatoryRules(): ResponseEntity<List<FormattingRuleDto>> {
        val rules = formattingConfigService.getAvailableRules()
        return ResponseEntity.ok(rules)
    }

    /*
    POST   /api/v1/formatting
        Start a formatting job
        body:
            {
                snippetId:{snippetId},
                snippet:{snippet}
                rules: {
                    rule1:{value1},
                    rule2:{value2},
                    ...
                }
            }
        response:
            {
                jobId:{jobId},
                snippetId:{snippetId},
            }
    */
    @PostMapping("/")
    fun startJob(
        @RequestBody request: FormattingConfigDto
    ) {
    }

    /*
    GET    /api/v1/formatting/{jobId}
        Get the formatting job status:
        {
            jobId:{jobId}
            snippetId:{snippetId},
            status:DONE/ERROR/PENDING/FORMATTING/CANCELED
        }
    */
    @GetMapping("/{jobId}")
    fun getJobStatus(
        @PathVariable jobId: String
    ): ResponseEntity<FormattingJobStatusResponse> {
    }

    /*
        DELETE /api/v1/formatting/{jobId}
        Cancel a formatting job
    */
    @DeleteMapping("/{jobId}")
    fun cancelJob(
        @PathVariable jobId: String
    ): ResponseEntity<*> {
    }

}
