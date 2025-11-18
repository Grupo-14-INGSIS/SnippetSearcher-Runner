package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.ValidationResponse
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetService
import com.grupo14IngSis.snippetSearcherRunner.service.SnippetValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/snippets")
class RunnerController(private val snippetService: SnippetService) {

    @PostMapping("/validate") // <-- NUEVA RUTA
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content si es OK
    fun validate(@RequestBody request: SnippetCreationRequest) {
        // La validación lanzará la excepción 400 si falla.
        snippetService.validate(request)
    }

    // Captura la excepción de validación y asegura que la respuesta HTTP sea 400
    @ExceptionHandler(SnippetValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: SnippetValidationException): ValidationResponse {
        // Devuelve el DTO de error, que el App capturará en su WebClient
        return ValidationResponse(
            message = e.message,
            rule = e.rule,
            line = e.line,
            column = e.column
        )
    }
}