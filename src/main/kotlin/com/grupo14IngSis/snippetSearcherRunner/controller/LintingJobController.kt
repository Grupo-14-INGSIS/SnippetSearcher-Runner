package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJobRequest
import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJobStatusResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/linting")
class LintingJobController(
) {
    /*
    GET    /api/v1/linting/rules
    GET    /api/v1/linting/rules/configurable
    GET    /api/v1/linting/rules/mandatory
        Get linting rules. Configurable rules come with default values
    */

    @GetMapping("/rules")
    fun getAllLintingRules(): ResponseEntity<Map<String, Any>> {
    }

    @GetMapping("/rules/optional")
    fun getConfigurableLintingRules(): ResponseEntity<Map<String, Any>> {
    }

    @GetMapping("/rules/mandatory")
    fun getMandatoryLintingRules(): ResponseEntity<Map<String, Any>> {
    }

    /*
    POST   /api/v1/linting
        Start a linting job
        body:
            {
                snippetId:{snippetId},
                snippet:{snippet},
                rules: {
                    rule1:{value1},
                    rule2:{value2},
                    ...
                }
            }
        response:
            {
                jobId:{jobId},
                snippetId:{snippetId}
            }
    */

    @PostMapping("")
    fun createLintingJob(
        @RequestBody request: LintingJobRequest,
    ): ResponseEntity<String> {
    }

    /*
    GET    /api/v1/linting/{jobId}/status
        Get the status of a linting job:
        {
            jobId:{jobId}
            snippetId:{snippetId}
            status:DONE/ERROR/PENDING/LINTING/CANCELED
        }
    */

    @GetMapping("/{jobId}/status")
    fun getJobStatus(
        @PathVariable jobId: String,
    ): ResponseEntity<LintingJobStatusResponse> {
    }

    /*
    DELETE /api/v1/linting/{jobId}
        Cancel a linting job
     */
    @DeleteMapping("/{jobId}")
    fun cancelLinting(
        @PathVariable jobId: String,
    ): ResponseEntity<*> {
    }
}