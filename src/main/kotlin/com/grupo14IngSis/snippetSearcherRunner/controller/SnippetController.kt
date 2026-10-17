package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.dto.GetSnippetResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorageException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * Contenido (código) de los snippets. El código vive en el asset service (ver [SnippetStorage]);
 * los metadatos (nombre, lenguaje, versión, compliance) viven en la base del App.
 */
@RestController
@RequestMapping("/api/v1/snippets")
class SnippetController(
    private val storage: SnippetStorage,
    private val appClient: AppClient,
    private val lintingService: LintingService,
    private val formattingService: FormattingService,
    private val analyzerPlugin: AnalyzerPlugin,
    private val formattingPlugin: FormattingPlugin,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    /**
     * GET    /api/v1/snippets/{snippetId}
     *
     * Fetch the content of a snippet.
     *
     * NOTA: este endpoint NO debe llamar a App. App (`GET /api/v1/snippets/{id}`) llama a este endpoint
     * para obtener el contenido; si desde acá volviéramos a llamar a App se genera una recursión infinita
     * App -> Runner -> App -> ... que agota los threads de ambos servicios y termina en 500.
     */
    @GetMapping("/{snippetId}")
    fun getSnippet(
        @PathVariable snippetId: String,
    ): ResponseEntity<*> {
        val content =
            storage.get(snippetId)
                ?: return ResponseEntity.status(404).body("Snippet with id $snippetId not found")
        return ResponseEntity.ok().body(GetSnippetResponse(snippetId, content))
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}
     *
     * Create a snippet: validates the code, uploads it to the bucket and registers it in App
     * (App queues its first lint).
     */
    @PutMapping("/{snippetId}")
    fun putSnippet(
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<Any> {
        val version = if (!request.version.isNullOrBlank()) request.version else "1.1"
        validate(request.snippet, version)?.let { return ResponseEntity.badRequest().body(it) }

        if (storage.exists(snippetId)) {
            return ResponseEntity.badRequest().body("Snippet $snippetId already exists.")
        }
        try {
            // Código escrito por el usuario: contenido actual + copia del original (US #13)
            storage.saveUserCode(snippetId, request.snippet)
        } catch (e: SnippetStorageException) {
            // Sin contenido guardado no se registra en App: no quedan snippets "fantasma"
            logger.error(e.message)
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Could not store snippet content.")
        }
        appClient.registerSnippet(snippetId, request.userId, request.name, request.language, version, request.description ?: "")
        // Se devuelve el snippet creado (id, name, content, language) como JSON: es el
        // contrato que esperan los clientes y los tests e2e de la UI.
        return ResponseEntity
            .created(URI.create("/api/v1/snippets/$snippetId"))
            .body(
                mapOf(
                    "id" to snippetId,
                    "name" to request.name,
                    "content" to request.snippet,
                    "language" to request.language,
                    "version" to version,
                    "message" to "Snippet created.",
                ),
            )
    }

    /**
     * PATCH    /api/v1/snippets/{snippetId}
     *
     * Update the content of a snippet, ask App to lint it again and run its tests.
     */
    @PatchMapping("/{snippetId}")
    fun patchSnippet(
        @PathVariable snippetId: String,
        @RequestBody request: SnippetUpdateRequest,
    ): ResponseEntity<Any> {
        val version = request.version ?: "1.1"
        validate(request.snippet, version)?.let { return ResponseEntity.badRequest().body(it) }

        // El asset se crea o reemplaza (upsert): si el snippet quedó registrado en App pero su
        // contenido nunca llegó al asset service, el PATCH tiene que poder guardarlo igual.
        // Lo que escribe el usuario pasa a ser también el nuevo "original" (US #13).
        try {
            storage.saveUserCode(snippetId, request.snippet)
        } catch (e: SnippetStorageException) {
            logger.error(e.message)
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Could not store snippet content.")
        }
        if (request.jwt.isNullOrBlank()) {
            return ResponseEntity.ok().body("Snippet updated successfully, but could not run tests.")
        }
        // El contenido cambió: el compliance anterior ya no vale. App lo pasa a "pending" y lo encola.
        try {
            appClient.requestLint(snippetId, request.jwt)
        } catch (e: Exception) {
            logger.warn("Could not request lint for snippet $snippetId: ${e.message}")
        }
        val results =
            try {
                appClient.testAll(snippetId, request.jwt)
            } catch (e: Exception) {
                return ResponseEntity.ok().body("Snippet updated successfully, but tests could not be run: ${e.message}")
            }
        var message: String
        if (!results.isEmpty()) {
            message = " with the following test results:\n"
            for (result in results) {
                message = "$message\n- $result"
            }
        } else {
            message = "."
        }
        return ResponseEntity.ok().body("Snippet updated successfully$message")
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}
     *
     * Delete the content of a snippet from the bucket
     */
    @DeleteMapping("/{snippetId}")
    fun deleteSnippet(
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> =
        try {
            if (storage.delete(snippetId)) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.status(404).body("Snippet with id $snippetId not found.")
            }
        } catch (e: SnippetStorageException) {
            logger.error(e.message)
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error deleting snippet.")
        }

    /**
     * POST /api/v1/snippets/{snippetId}/tasks/{task}?userId=&language=&version=
     *
     * Applies a task (format / lint) to a snippet synchronously with the rules of [userId].
     * POST: es una acción con resultado, no un reemplazo. El formateo guarda el resultado en el bucket.
     * Body opcional (texto, solo lint): código a analizar en lugar del guardado (lo que hay en el editor).
     */
    @PostMapping("/{snippetId}/tasks/{task}")
    fun applyTask(
        @PathVariable snippetId: String,
        @PathVariable task: String,
        @RequestParam userId: String,
        @RequestParam(defaultValue = "printscript") language: String,
        @RequestParam(defaultValue = "1.1") version: String,
        @RequestBody(required = false) content: String? = null,
    ): ResponseEntity<String> {
        val isFormat = task.contains("format", ignoreCase = true)
        if (!isFormat && !task.contains("lint", ignoreCase = true)) {
            return ResponseEntity.badRequest().body("Unknown task: $task")
        }
        val snippet =
            (if (isFormat) null else content)
                ?: storage.get(snippetId)
                ?: return ResponseEntity.status(404).body("Snippet not found")
        // Antes se llamaba al plugin con config null y los dos plugins fallaban (500): necesitan las reglas
        val rules = if (isFormat) formattingService.getRules(userId, language) else lintingService.getRules(userId, language)
        if (rules.isEmpty()) {
            return ResponseEntity.badRequest().body("User has no ${if (isFormat) "formatting" else "linting"} rules for $language")
        }
        val config = rules + ("version" to version)

        // Los plugins no hacen I/O: si lanzan es por el código o la configuración, no un error del servidor
        @Suppress("TooGenericExceptionCaught")
        val output =
            try {
                if (isFormat) formattingPlugin.run(snippet, config) as String else analyzerPlugin.lint(snippet, config).output
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message ?: "Could not apply $task")
            }
        if (isFormat) {
            try {
                storage.save(snippetId, output)
            } catch (e: SnippetStorageException) {
                logger.error(e.message)
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Could not store formatted snippet.")
            }
        }
        return ResponseEntity.ok().body(output)
    }

    /**
     * GET /api/v1/snippets/{snippetId}/download?variant=original|formatted&userId=&language=&version=
     *
     * US #13: descarga del snippet. `original` es el código tal como lo escribió el usuario (aunque
     * después se haya formateado); `formatted` es el código formateado AHORA con las reglas de [userId]
     * (el dueño), sin guardar nada. Si el usuario no tiene reglas de formateo se devuelve el contenido actual.
     */
    @GetMapping("/{snippetId}/download", produces = ["text/plain;charset=UTF-8"])
    fun download(
        @PathVariable snippetId: String,
        @RequestParam(defaultValue = "original") variant: String,
        @RequestParam userId: String,
        @RequestParam(defaultValue = "printscript") language: String,
        @RequestParam(defaultValue = "1.1") version: String,
    ): ResponseEntity<String> {
        val formatted =
            when (variant.lowercase()) {
                "original" -> false
                "formatted" -> true
                else -> return ResponseEntity.badRequest().body("Unknown variant: $variant (use original or formatted)")
            }
        if (!formatted) {
            val original = storage.getOriginal(snippetId) ?: return ResponseEntity.status(404).body("Snippet not found")
            return ResponseEntity.ok(original)
        }
        val current = storage.get(snippetId) ?: return ResponseEntity.status(404).body("Snippet not found")
        val rules = formattingService.getRules(userId, language)
        if (rules.isEmpty()) return ResponseEntity.ok(current)
        @Suppress("TooGenericExceptionCaught")
        return try {
            ResponseEntity.ok(formattingPlugin.run(current, rules + ("version" to version)) as String)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "Could not format snippet")
        }
    }

    /** Devuelve el mensaje de error si el código no es válido para esa versión, o null si lo es. */
    private fun validate(
        snippet: String,
        version: String,
    ): String? {
        val validationResult = ValidationPlugin().run(snippet, mapOf("version" to version)) as String
        val hasError =
            validationResult.contains("ERROR", ignoreCase = true) ||
                validationResult.contains("Exception", ignoreCase = true) ||
                validationResult.contains("Syntax error", ignoreCase = true) ||
                validationResult.contains("Parsing error", ignoreCase = true)
        return if (hasError) validationResult else null
    }
}
