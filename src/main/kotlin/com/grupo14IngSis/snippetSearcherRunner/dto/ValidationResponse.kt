package com.grupo14IngSis.snippetSearcherRunner.dto

// DTO para manejar errores detallados de validación (usado por el Runner y atrapado por el App)
data class ValidationResponse(
    val isValid: Boolean,
    val message: String,
    val rule: String?,
    val line: Int?,
    val column: Int?,
)
