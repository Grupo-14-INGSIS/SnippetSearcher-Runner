package com.grupo14lngSis.snippetSearcherRunner.testing

import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingRequest
import com.grupo14lngSis.snippetSearcherRunner.testing.dto.TestingResult
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class TestingJobRepository {
    private val testingJobs = ConcurrentHashMap<String, TestingRequest>()
    private val testingResults = ConcurrentHashMap<String, TestingResult>()

    fun save(request: TestingRequest): String {
        testingJobs[request.snippetId] = request
        return request.snippetId
    }

    fun saveResult(
        snippetId: String,
        result: TestingResult,
    ) {
        testingResults[snippetId] = result
    }

    fun getResult(snippetId: String): TestingResult? {
        return testingResults[snippetId]
    }

    fun getRequest(snippetId: String): TestingRequest? {
        return testingJobs[snippetId]
    }
}
