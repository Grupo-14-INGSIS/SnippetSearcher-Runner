package com.grupo14IngSis.snippetSearcherRunner.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.testing.dto.StartTestRequest
import com.grupo14IngSis.snippetSearcherRunner.testing.dto.StartTestResponse
import com.grupo14IngSis.snippetSearcherRunner.testing.dto.TestStatusResponse
import org.springframework.web.bind.annotation.*

import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/v1/testing")
class TestingJobController(
    executionService: SnippetTestingService
) {

    /*
    POST   /api/v1/testing
        Start a testing job
        body:
            {
                snippetId:{snippetId}
                snippet:{snippet}
                input:{input}
                expected:{expected}
            }
        response:
            {
                jobId:{jobId},
                snippetId:{snippetId}
                input:{input}
                expected:{expected}
            }
    */
    @PostMapping("")
    fun startTest(
        @RequestBody request: StartTestRequest
    ): ResponseEntity<StartTestResponse> {

    }

    /*
    GET    /api/v1/testing/{jobId}
        Get testing job status:
        {
            jobId:{jobId},
            snippetId:{snippetId}
            input:{input}
            expected:{expected}
            actual:{actual_or_empty}
            status:OK/FAILED/TESTING/CANCELED/ERROR/PENDING
        }
    */
    @GetMapping("{jobId}")
    fun getTestStatus(
        @PathVariable("jobId") jobId: String,
    ): ResponseEntity<TestStatusResponse> {

    }

    /*
    DELETE /api/v1/testing/{jobId}
        Cancel the execution of a testing job
     */
    @DeleteMapping("{jobId}")
    fun CancelTestingJob(
        @PathVariable("jobId") jobId: String,
    ): ResponseEntity<*> {

    }
}