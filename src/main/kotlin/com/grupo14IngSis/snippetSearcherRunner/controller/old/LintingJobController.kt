//package com.grupo14IngSis.snippetSearcherRunner.controller
//
//import com.grupo14IngSis.snippetSearcherRunner.linting.LintingJobService
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJob
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/api/linting-jobs")
//class LintingJobController(
//    private val lintingJobService: LintingJobService,
//) {
//    /**
//     * GET /api/linting-jobs/{jobId}
//     * Obtiene el estado y resultados de un trabajo de linting
//     */
//    @GetMapping("/{jobId}")
//    fun getJobStatus(
//        @PathVariable jobId: String,
//    ): ResponseEntity<LintingJob> {
//        return lintingJobService.getJobStatus(jobId)
//            ?.let { ResponseEntity.ok(it) }
//            ?: ResponseEntity.notFound().build()
//    }
//}
