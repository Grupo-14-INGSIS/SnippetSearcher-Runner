package com.grupo14IngSis.snippetSearcherRunner.client

import com.grupo14IngSis.snippetSearcherRunner.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class AppClientTestAllTest {
    private lateinit var restTemplate: RestTemplate
    private lateinit var appClient: AppClient
    private val app = "http://app:8080"

    @BeforeEach
    fun setUp() {
        restTemplate = mockk(relaxed = true)
        appClient = AppClient(restTemplate, app)
    }

    @AfterEach
    fun tearDown() = MDC.clear()

    private fun testsBody(body: Any?) {
        every {
            restTemplate.exchange("$app/api/v1/snippets/s1/tests", HttpMethod.GET, any<HttpEntity<Void>>(), Any::class.java)
        } returns ResponseEntity.ok(body)
    }

    private fun runResult(
        testId: String,
        response: TestResponse?,
    ) {
        every {
            restTemplate.exchange(
                "$app/api/v1/snippets/s1/tests/$testId/runs",
                HttpMethod.POST,
                any<HttpEntity<Void>>(),
                TestResponse::class.java,
            )
        } returns ResponseEntity.ok(response)
    }

    @Test
    fun `testAll devuelve solo los mensajes de los tests que fallan, con el mapa de App`() {
        testsBody(mapOf("t1" to mapOf<String, Any>(), "t2" to mapOf<String, Any>()))
        runResult("t1", TestResponse(listOf("ok"), TestResult.SUCCESS, "paso"))
        runResult("t2", TestResponse(listOf("mal"), TestResult.FAILED, "esperaba ok"))

        assertEquals(listOf("esperaba ok"), appClient.testAll("s1", "jwt"))
        verify {
            restTemplate.exchange(
                "$app/api/v1/snippets/s1/tests",
                HttpMethod.GET,
                match<HttpEntity<Void>> { it.headers.getFirst("Authorization") == "Bearer jwt" },
                Any::class.java,
            )
        }
    }

    @Test
    fun `testAll acepta una lista de ids o de objetos con testId o id`() {
        testsBody(listOf(mapOf("testId" to "a"), mapOf("id" to "b"), "c"))
        runResult("a", TestResponse(emptyList(), TestResult.ERROR, "error a"))
        runResult("b", TestResponse(emptyList(), TestResult.SUCCESS, "ok"))
        runResult("c", TestResponse(emptyList(), TestResult.FAILED, "falla c"))

        assertEquals(listOf("error a", "falla c"), appClient.testAll("s1", "jwt"))
    }

    @Test
    fun `testAll ignora corridas sin cuerpo`() {
        testsBody(listOf("a"))
        runResult("a", null)

        assertTrue(appClient.testAll("s1", "jwt").isEmpty())
    }

    @Test
    fun `testAll sin tests o con una respuesta inesperada devuelve vacio`() {
        testsBody(null)
        assertTrue(appClient.testAll("s1", "jwt").isEmpty())

        testsBody("respuesta rara")
        assertTrue(appClient.testAll("s1", "jwt").isEmpty())
    }

    @Test
    fun `registerSnippet reenvia el requestId del MDC`() {
        MDC.put("requestId", "req-123")
        every {
            restTemplate.exchange(any<String>(), HttpMethod.PUT, any<HttpEntity<*>>(), SnippetCreationResponse::class.java)
        } returns ResponseEntity.ok(SnippetCreationResponse(true, "ok"))

        appClient.registerSnippet("s1", "u1", "nombre", "printscript")

        verify {
            restTemplate.exchange(
                "$app/internal/snippets/s1",
                HttpMethod.PUT,
                match<HttpEntity<*>> { it.headers.getFirst("X-Request-Id") == "req-123" },
                SnippetCreationResponse::class.java,
            )
        }
    }
}
