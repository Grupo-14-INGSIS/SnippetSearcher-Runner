package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherRunner.model.Snippet
import org.springframework.stereotype.Service

// Modelo interno para el resultado de la validación
data class ValidationResult(
    val isValid: Boolean,
    val rule: String? = null,
    val line: Int? = null,
    val column: Int? = null,
)

@Service
class SnippetService(/* private val snippetRepository: SnippetRepository */) {
    // Simulación de base de datos en memoria
    private val snippetsDB = mutableMapOf<Long, Snippet>()
    private var nextId = 1L

    // ========== CREAR SNIPPET ==========
    fun validateAndSave(request: SnippetCreationRequest): SnippetCreationResponse {
        // 1. Validación del Snippet (Lógica del parser)
        val validationResult = validateCode(request.code, request.language)

        if (!validationResult.isValid) {
            // Si es inválido, lanzamos la excepción con los detalles
            throw SnippetValidationException(
                message = "El código no es válido según el parser.",
                rule = validationResult.rule,
                line = validationResult.line,
                column = validationResult.column,
            )
        }

        // 2. Persistencia (Simulación en memoria)
        val snippetId = nextId++
        val snippet =
            Snippet(
                id = snippetId,
                name = request.name,
                description = request.description,
                language = request.language,
                version = "1.0",
                code = request.code,
                ownerId = request.userId,
            )
        snippetsDB[snippetId] = snippet

        // 3. Respuesta Exitosa
        return SnippetCreationResponse(
            success = true,
            message = "Snippet '${request.name}' creado y guardado con ID: $snippetId",
        )
    }

    // ========== ACTUALIZAR SNIPPET ==========
    fun validateAndUpdate(
        snippetId: Long,
        userId: String,
        updateRequest: SnippetUpdateRequest,
    ): SnippetUpdateResponse {
        // 1. Verificar que el snippet existe
        val existingSnippet =
            snippetsDB[snippetId]
                ?: throw IllegalArgumentException("Snippet con ID $snippetId no encontrado")

        // 2. Verificar permisos (que el usuario sea el dueño)
        if (existingSnippet.ownerId != userId) {
            throw SecurityException("No tienes permisos para editar este snippet")
        }

        // 3. Validar el nuevo contenido si se está actualizando
        val newContent = updateRequest.content ?: existingSnippet.code
        val newLanguage = updateRequest.language ?: existingSnippet.language

        val validationResult = validateCode(newContent, newLanguage)

        if (!validationResult.isValid) {
            // Si es inválido, devolver respuesta con errores
            return SnippetUpdateResponse(
                id = snippetId.toString(),
                name = existingSnippet.name,
                description = existingSnippet.description,
                language = existingSnippet.language,
                version = existingSnippet.version,
                content = existingSnippet.code,
                isValid = false,
                validationErrors =
                    listOf(
                        "Error en línea ${validationResult.line}, columna ${validationResult.column}: ${validationResult.rule}",
                    ),
            )
        }

        // 4. Actualizar el snippet
        val updatedSnippet =
            existingSnippet.copy(
                name = updateRequest.name ?: existingSnippet.name,
                description = updateRequest.description ?: existingSnippet.description,
                language = newLanguage,
                version = updateRequest.version ?: existingSnippet.version,
                code = newContent,
            )
        snippetsDB[snippetId] = updatedSnippet

        // 5. Respuesta exitosa
        return SnippetUpdateResponse(
            id = snippetId.toString(),
            name = updatedSnippet.name,
            description = updatedSnippet.description,
            language = updatedSnippet.language,
            version = updatedSnippet.version,
            content = updatedSnippet.code,
            isValid = true,
            validationErrors = null,
        )
    }

    // ========== OBTENER SNIPPET POR ID ==========
    fun getSnippetById(
        snippetId: Long,
        userId: String,
    ): Snippet {
        // 1. Verificar que el snippet existe
        val snippet =
            snippetsDB[snippetId]
                ?: throw IllegalArgumentException("Snippet con ID $snippetId no encontrado")

        // 2. Verificar permisos (que el usuario sea el dueño o tenga acceso)
        // TODO: Implementar lógica de permisos más compleja si es necesario
        if (snippet.ownerId != userId) {
            throw SecurityException("No tienes permisos para ver este snippet")
        }

        return snippet
    }

    // ========== OBTENER TODOS LOS SNIPPETS DEL USUARIO ==========
    fun getAllSnippetsByUser(userId: String): List<Snippet> {
        // Filtrar snippets que pertenecen al usuario
        return snippetsDB.values.filter { it.ownerId == userId }
    }

    // ========== VALIDACIÓN DE CÓDIGO ==========
    // Simulación del parser según las "Notas" del caso de uso
    private fun validateCode(
        code: String,
        language: String,
    ): ValidationResult {
        // Simulación: Falla si el código contiene una palabra prohibida
        if (code.contains("prohibited_call", ignoreCase = true)) {
            return ValidationResult(
                isValid = false,
                rule = "Uso de 'prohibited_call' no permitido en $language",
                line = 10,
                column = 5,
            )
        }

        // Validación básica: código no puede estar vacío
        if (code.isBlank()) {
            return ValidationResult(
                isValid = false,
                rule = "El código no puede estar vacío",
                line = 1,
                column = 1,
            )
        }

        return ValidationResult(true)
    }
}
