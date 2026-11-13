package com.grupo14IngSis.snippetSearcherRunner.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * GET /api/v1/version
 *
 * Versión de las librerías de PrintScript con las que corre este Runner. App la compara con la última que
 * vio: si cambió (parser nuevo), vuelve a validar y lintear todos los snippets.
 */
@RestController
@RequestMapping("/api/v1/version")
class VersionController(
    @Value("\${printscript.version:unknown}") private val printscriptVersion: String,
) {
    @GetMapping
    fun version(): ResponseEntity<Map<String, String>> = ResponseEntity.ok(mapOf("printscript" to printscriptVersion))
}
