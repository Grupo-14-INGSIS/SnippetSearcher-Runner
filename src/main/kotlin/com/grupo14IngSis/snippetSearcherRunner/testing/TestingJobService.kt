//package com.grupo14lngSis.snippetSearcherRunner.testing
//
//import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingRequest
//import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingResult
//import org.springframework.stereotype.Service
//
//@Service
//class TestingJobService(
//    private val testingJobProcessor: TestingJobProcessor,
//    private val testingJobRepository: TestingJobRepository,
//) {
//    fun submitTestingJob(request: TestingRequest): String {
//        val jobId = testingJobRepository.save(request)
//        return jobId
//    }
//
//    fun executeTestingJob(request: TestingRequest): TestingResult {
//        val result = testingJobProcessor.processTestingJob(request)
//        testingJobRepository.saveResult(request.snippetId, result)
//        return result
//    }
//
//    fun getTestingResult(snippetId: String): TestingResult? {
//        return testingJobRepository.getResult(snippetId)
//    }
//}
