package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.ValidationResponse
import com.grupo14IngSis.snippetSearcherRunner.model.Snippet
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetService
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/snippets")
class RunnerController(private val snippetService: SnippetService) {
    // ========== CREAR SNIPPET ==========
    @PostMapping
    fun processAndSave(
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<SnippetCreationResponse> {
        // La llamada puede lanzar SnippetValidationException
        val response = snippetService.validateAndSave(request)

        // Si no hay excepción, devuelve 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ========== ACTUALIZAR SNIPPET ==========
    @PutMapping("/{snippetId}")
    fun updateSnippet(
        @PathVariable snippetId: Long,
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody updateRequest: SnippetUpdateRequest,
    ): ResponseEntity<SnippetUpdateResponse> {
        // Validar y actualizar el snippet
        val response = snippetService.validateAndUpdate(snippetId, userId, updateRequest)

        return ResponseEntity.ok(response)
    }

    // ========== OBTENER SNIPPET POR ID ==========
    @GetMapping("/{snippetId}")
    fun getSnippet(
        @PathVariable snippetId: Long,
        @RequestHeader("X-User-Id") userId: String,
    ): ResponseEntity<Snippet> {
        val snippet = snippetService.getSnippetById(snippetId, userId)

        return ResponseEntity.ok(snippet)
    }

    // ========== OBTENER TODOS LOS SNIPPETS DEL USUARIO ==========
    @GetMapping
    fun getAllSnippets(
        @RequestHeader("X-User-Id") userId: String,
    ): ResponseEntity<List<Snippet>> {
        val snippets = snippetService.getAllSnippetsByUser(userId)

        return ResponseEntity.ok(snippets)
    }

    // ========== MANEJADOR DE EXCEPCIONES ==========
    @ExceptionHandler(SnippetValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: SnippetValidationException): ValidationResponse {
        // Devuelve el DTO de error, que el App capturará en su WebClient
        return ValidationResponse(
            isValid = false,
            message = e.message ?: "Error de validación",
            rule = e.rule,
            line = e.line,
            column = e.column,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: IllegalArgumentException): Map<String, String> {
        return mapOf("error" to (e.message ?: "Recurso no encontrado"))
    }

    @ExceptionHandler(SecurityException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleSecurityException(e: SecurityException): Map<String, String> {
        return mapOf("error" to (e.message ?: "Sin permisos"))
    }
}
