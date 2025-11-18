package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import org.springframework.stereotype.Service

// Modelo interno para el resultado de la validación
data class ValidationResult(
    val isValid: Boolean,
    val rule: String? = null,
    val line: Int? = null,
    val column: Int? = null
)

@Service
class SnippetService(/* private val snippetRepository: SnippetRepository */) {

    fun validate(request: SnippetCreationRequest) {
        val validationResult = validateCode(request.code, request.language)

        if (!validationResult.isValid) {
            throw SnippetValidationException("Snippet not validated", "To be implemented", 0, 0)
        }
        // No hay código de persistencia
    }

    // Reemplazar por lógica del Parser
    private fun validateCode(code: String, language: String): ValidationResult {
        // Simulación: Falla si el código contiene una palabra prohibida
        if (code.contains("prohibited_call", ignoreCase = true)) {
            return ValidationResult(
                isValid = false,
                rule = "Uso de 'prohibited_call' no permitido en $language",
                line = 10,
                column = 5
            )
        }
        return ValidationResult(true)
    }
}