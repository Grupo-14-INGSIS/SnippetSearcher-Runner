package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.*
import com.grupo14IngSis.snippetSearcherRunner.service.TestService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class TestController(
    private val testService: TestService
) {

    // Crear test para un snippet
    @PostMapping("/snippets/{snippetId}/tests")
    fun createTest(
        @PathVariable snippetId: Long,
        @RequestParam userId: String,
        @RequestBody request: CreateTestRequest
    ): ResponseEntity<TestCaseDto> {
        return try {
            val test = testService.createTest(snippetId, userId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(test)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    // Obtener todos los tests de un snippet
    @GetMapping("/snippets/{snippetId}/tests")
    fun getTestsBySnippet(
        @PathVariable snippetId: Long,
        @RequestParam userId: String
    ): ResponseEntity<List<TestCaseDto>> {
        return try {
            val tests = testService.getTestsBySnippet(snippetId, userId)
            ResponseEntity.ok(tests)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Obtener un test específico
    @GetMapping("/tests/{testId}")
    fun getTest(
        @PathVariable testId: String,
        @RequestParam userId: String
    ): ResponseEntity<TestCaseDto> {
        return try {
            val test = testService.getTest(testId, userId)
            ResponseEntity.ok(test)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Actualizar un test
    @PutMapping("/tests/{testId}")
    fun updateTest(
        @PathVariable testId: String,
        @RequestParam userId: String,
        @RequestBody request: UpdateTestRequest
    ): ResponseEntity<TestCaseDto> {
        return try {
            val test = testService.updateTest(testId, userId, request)
            ResponseEntity.ok(test)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Eliminar un test
    @DeleteMapping("/tests/{testId}")
    fun deleteTest(
        @PathVariable testId: String,
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        return try {
            testService.deleteTest(testId, userId)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // ========== USER STORY #9: EJECUTAR TEST ==========

    // Ejecutar un test específico (muestra output paso a paso)
    @PostMapping("/snippets/{snippetId}/tests/{testId}/run")
    fun runTest(
        @PathVariable snippetId: Long,
        @PathVariable testId: String,
        @RequestParam userId: String
    ): ResponseEntity<TestResultDto> {
        return try {
            val result = testService.runTest(snippetId, testId, userId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // Ejecutar todos los tests de un snippet
    @PostMapping("/snippets/{snippetId}/tests/run-all")
    fun runAllTests(
        @PathVariable snippetId: Long,
        @RequestParam userId: String
    ): ResponseEntity<List<TestResultDto>> {
        return try {
            val results = testService.runAllTests(snippetId, userId)
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}