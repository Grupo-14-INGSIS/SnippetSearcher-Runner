package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.GetSnippetResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintOutcome
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintReport
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SnippetControllerTest {
    private val assetServiceClient: AssetServiceClient = mockk()
    private val appClient: AppClient = mockk(relaxed = true)
    private val lintingService: LintingService = mockk()
    private val formattingService: FormattingService = mockk()
    private val analyzer: AnalyzerPlugin = mockk()
    private val formatter: FormattingPlugin = mockk()
    private val snippetController =
        SnippetController(SnippetStorage(assetServiceClient), appClient, lintingService, formattingService, analyzer, formatter)
    private val rules = mapOf<String, Any>("identifier_format" to true)

    @Test
    fun `format task uses the user rules and saves the result`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a:number=1;"
        every { formattingService.getRules("u1", "printscript") } returns rules
        every { formatter.run("let a:number=1;", rules + ("version" to "1.1")) } returns "let a : number = 1;"
        every { assetServiceClient.postAsset(container, snippetId, "let a : number = 1;") } returns 201

        val response = snippetController.applyTask(snippetId, "format", "u1", "printscript", "1.1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("let a : number = 1;", response.body)
        verify { assetServiceClient.postAsset(container, snippetId, "let a : number = 1;") }
    }

    @Test
    fun `lint task uses the user rules and does not touch the bucket`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a: number = 1;"
        every { lintingService.getRules("u1", "printscript") } returns rules
        every { analyzer.lint(any(), rules + ("version" to "1.1")) } returns LintReport(LintOutcome.COMPLIANT, "SUCCESS")

        val response = snippetController.applyTask(snippetId, "lint", "u1", "printscript", "1.1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("SUCCESS", response.body)
        verify(exactly = 0) { assetServiceClient.postAsset(any(), any(), any()) }
    }

    @Test
    fun `lint task with a body lints that content instead of the stored one`() {
        // El botón Lint de la UI manda lo que hay en el editor, aunque no esté guardado
        every { lintingService.getRules("u1", "printscript") } returns rules
        every { analyzer.lint("println(a + b);", rules + ("version" to "1.1")) } returns
            LintReport(LintOutcome.NOT_COMPLIANT, "ANALYSIS RESULTS: 1 issue(s) found")

        val response = snippetController.applyTask(snippetId, "lint", "u1", "printscript", "1.1", "println(a + b);")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("ANALYSIS RESULTS: 1 issue(s) found", response.body)
        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
    }

    @Test
    fun `format task ignores a body and formats the stored content`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a:number=1;"
        every { formattingService.getRules("u1", "printscript") } returns rules
        every { formatter.run("let a:number=1;", any()) } returns "let a : number = 1;"
        every { assetServiceClient.postAsset(container, snippetId, "let a : number = 1;") } returns 201

        val response = snippetController.applyTask(snippetId, "format", "u1", "printscript", "1.1", "otro codigo")

        assertEquals("let a : number = 1;", response.body)
    }

    @Test
    fun `task without rules is a 400, not a 500`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "code"
        every { formattingService.getRules("u1", "printscript") } returns emptyMap()

        val response = snippetController.applyTask(snippetId, "format", "u1", "printscript", "1.1")

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `task on code the plugin cannot process is a 400 and nothing is saved`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a: number = 1"
        every { formattingService.getRules("u1", "printscript") } returns rules
        every { formatter.run(any(), any()) } throws RuntimeException("Statement must end with a semicolon")

        val response = snippetController.applyTask(snippetId, "format", "u1", "printscript", "1.1")

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Statement must end with a semicolon", response.body)
        verify(exactly = 0) { assetServiceClient.postAsset(any(), any(), any()) }
    }

    @Test
    fun `unknown task is a 400 and missing snippet is a 404`() {
        assertEquals(HttpStatus.BAD_REQUEST, snippetController.applyTask(snippetId, "compile", "u1", "printscript", "1.1").statusCode)

        every { assetServiceClient.getAsset(container, snippetId) } returns null
        assertEquals(HttpStatus.NOT_FOUND, snippetController.applyTask(snippetId, "lint", "u1", "printscript", "1.1").statusCode)
    }

    // Todos los snippets viven en un único container del bucket
    private val container = SnippetStorage.CONTAINER
    private val snippetId = "test-snippet"

    @Test
    fun `getSnippet should return snippet content when asset exists`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "Hello, world!"

        val response = snippetController.getSnippet(snippetId) as ResponseEntity<GetSnippetResponse>

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Hello, world!", response.body?.content)
    }

    @Test
    fun `getSnippet should return 404 when asset does not exist`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns null

        val response = snippetController.getSnippet(snippetId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Snippet with id $snippetId not found", response.body)
    }

    @Test
    fun `patchSnippet saves the content and asks App to lint it again`() {
        val snippetContent = "println(\"Updated content\");"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 200
        every { assetServiceClient.postAsset(container, original, snippetContent) } returns 200
        every { appClient.testAll(snippetId, any()) } returns listOf()

        val response = snippetController.patchSnippet(snippetId, SnippetUpdateRequest("the-jwt", snippetContent))

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 1) { appClient.requestLint(snippetId, "the-jwt") }
        // Lo que escribe el usuario es el nuevo original (US #13)
        verify { assetServiceClient.postAsset(container, original, snippetContent) }
    }

    private val original = SnippetStorage.originalKey("test-snippet")

    @Test
    fun `download original returns the code the user wrote even after formatting`() {
        every { assetServiceClient.getAsset(container, original) } returns "let a:number=1;"

        val response = snippetController.download(snippetId, "original", "u1", "printscript", "1.1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("let a:number=1;", response.body)
    }

    @Test
    fun `download original of an old snippet without copy falls back to the current content`() {
        every { assetServiceClient.getAsset(container, original) } returns null
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a: number = 1;"

        assertEquals("let a: number = 1;", snippetController.download(snippetId, "original", "u1", "printscript", "1.1").body)
    }

    @Test
    fun `download formatted formats now with the owner rules and saves nothing`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "let a:number=1;"
        every { formattingService.getRules("u1", "printscript") } returns rules
        every { formatter.run("let a:number=1;", rules + ("version" to "1.1")) } returns "let a : number = 1;"

        val response = snippetController.download(snippetId, "formatted", "u1", "printscript", "1.1")

        assertEquals("let a : number = 1;", response.body)
        verify(exactly = 0) { assetServiceClient.postAsset(any(), any(), any()) }
    }

    @Test
    fun `download formatted without formatting rules returns the current content`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "code"
        every { formattingService.getRules("u1", "printscript") } returns emptyMap()

        assertEquals("code", snippetController.download(snippetId, "formatted", "u1", "printscript", "1.1").body)
    }

    @Test
    fun `download with an unknown variant is 400 and a missing snippet is 404`() {
        assertEquals(HttpStatus.BAD_REQUEST, snippetController.download(snippetId, "zip", "u1", "printscript", "1.1").statusCode)

        every { assetServiceClient.getAsset(container, any()) } returns null
        assertEquals(HttpStatus.NOT_FOUND, snippetController.download(snippetId, "original", "u1", "printscript", "1.1").statusCode)
        assertEquals(HttpStatus.NOT_FOUND, snippetController.download(snippetId, "formatted", "u1", "printscript", "1.1").statusCode)
    }

    @Test
    fun `patchSnippet without JWT saves but cannot request lint or run tests`() {
        val snippetContent = "println(\"Updated content\");"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 200
        every { assetServiceClient.postAsset(container, original, snippetContent) } returns 200

        val response = snippetController.patchSnippet(snippetId, SnippetUpdateRequest(null, snippetContent))

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 0) { appClient.requestLint(any(), any()) }
    }

    @Test
    fun `patchSnippet returns 502 when the bucket does not store the content`() {
        val snippetContent = "println(\"Updated content\");"
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 500

        val response = snippetController.patchSnippet(snippetId, SnippetUpdateRequest("jwt", snippetContent))

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        verify(exactly = 0) { appClient.requestLint(any(), any()) }
    }

    @Test
    fun `putSnippet should return 201 when creating a new snippet`() {
        val snippetContent = "println(\"New content\");"
        every { assetServiceClient.getAsset(container, snippetId) } returns null
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 201
        every { assetServiceClient.postAsset(container, original, snippetContent) } returns 201

        val request = SnippetCreationRequest("test-user", "script", "printscript", snippetContent)
        val response: ResponseEntity<Any> = snippetController.putSnippet(snippetId, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        verify { assetServiceClient.postAsset(container, original, snippetContent) }
        verify(exactly = 1) { appClient.registerSnippet(snippetId, "test-user", "script", "printscript", "1.1", "") }
    }

    @Test
    fun `putSnippet does not register in App when the bucket fails`() {
        val snippetContent = "println(\"New content\");"
        every { assetServiceClient.getAsset(container, snippetId) } returns null
        every { assetServiceClient.postAsset(container, snippetId, snippetContent) } returns 500

        val request = SnippetCreationRequest("test-user", "script", "printscript", snippetContent)
        val response: ResponseEntity<Any> = snippetController.putSnippet(snippetId, request)

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        verify(exactly = 0) { appClient.registerSnippet(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `putSnippet returns 400 when the snippet already exists`() {
        every { assetServiceClient.getAsset(container, snippetId) } returns "existing"

        val request = SnippetCreationRequest("test-user", "script", "printscript", "println(\"x\");")
        val response: ResponseEntity<Any> = snippetController.putSnippet(snippetId, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `putSnippet returns 400 for invalid code`() {
        val request = SnippetCreationRequest("test-user", "script", "printscript", "New content")
        val response: ResponseEntity<Any> = snippetController.putSnippet(snippetId, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `deleteSnippet should return 204 when snippet is deleted successfully`() {
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 204

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(snippetId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `deleteSnippet should return 404 when snippet to delete is not found`() {
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 404

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(snippetId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Snippet with id $snippetId not found.", response.body)
    }

    @Test
    fun `deleteSnippet should return error status when deletion fails`() {
        every { assetServiceClient.deleteAsset(container, snippetId) } returns 500

        val response: ResponseEntity<Any> = snippetController.deleteSnippet(snippetId)

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        assertEquals("Error deleting snippet.", response.body)
    }
}
