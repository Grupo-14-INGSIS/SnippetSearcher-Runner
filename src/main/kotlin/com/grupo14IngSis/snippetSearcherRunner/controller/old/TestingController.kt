//package com.grupo14lngSis.snippetSearcherRunner.controller
//
//import com.grupo14lngSis.snippetSearcherRunner.testing.TestingJobService
//import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingRequest
//import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingResult
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/api/testing")
//class TestingController(
//    private val testingJobService: TestingJobService,
//) {
//    @PostMapping("/submit")
//    fun submitTests(
//        @RequestBody request: TestingRequest,
//    ): ResponseEntity<String> {
//        val jobId = testingJobService.submitTestingJob(request)
//        return ResponseEntity.ok(jobId)
//    }
//
//    @PostMapping("/execute")
//    fun executeTests(
//        @RequestBody request: TestingRequest,
//    ): ResponseEntity<TestingResult> {
//        val result = testingJobService.executeTestingJob(request)
//        return ResponseEntity.ok(result)
//    }
//
//    @GetMapping("/{snippetId}/results")
//    fun getResults(
//        @PathVariable snippetId: String,
//    ): ResponseEntity<TestingResult> {
//        val result = testingJobService.getTestingResult(snippetId)
//        return if (result != null) {
//            ResponseEntity.ok(result)
//        } else {
//            ResponseEntity.notFound().build()
//        }
//    }
//}
