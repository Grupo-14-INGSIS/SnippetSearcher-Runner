package com.grupo14IngSis.snippetSearcherRunner.dto

data class ExecutionRequest(
    val userId: String,
    val environment: Map<String, String>,
    val version: String = "1.0",
    /**
     * Inputs ya provistos por el usuario (en orden). La ejecución interactiva es stateless:
     * cada vez que el usuario ingresa un valor se vuelve a ejecutar el snippet con todos los
     * inputs acumulados, por lo que cualquier réplica del Runner puede atender la request.
     */
    val inputs: List<String> = emptyList(),
)
