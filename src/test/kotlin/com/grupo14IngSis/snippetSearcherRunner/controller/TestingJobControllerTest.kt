package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.TestRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import com.grupo14IngSis.snippetSearcherRunner.service.ExecutionProgressStore
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestingJobControllerTest {
    private val assetServiceClient: AssetServiceClient = mockk()
    private val progress: ExecutionProgressStore = mockk(relaxed = true)
    private val testingJobController = TestingJobController(SnippetStorage(assetServiceClient), progress)

    @Test
    fun `test output is published line by line while it runs, without prompts`() {
        every { progress.key(ExecutionProgressStore.TEST, "u1", "snippet-1") } returns "k"
        every { assetServiceClient.getAsset("snippets", "snippet-1") } returns
            "let n: string = readInput(\"name?\");\nprintln(\"hola \" + n);\nprintln(\"chau\");"
        val request = TestRequest("snippet-1", "u1", "1.1", emptyMap(), listOf("Juana"), listOf("hola Juana", "chau"))

        val response = testingJobController.testSnippet(request).body

        assertEquals(TestResult.SUCCESS, response?.result)
        verifyOrder {
            progress.start("k")
            progress.append("k", "hola Juana")
            progress.append("k", "chau")
            progress.finish("k", ExecutionEventType.COMPLETED)
        }
        // El prompt de readInput no es output del test
        verify(exactly = 0) { progress.append("k", "name?") }
    }

    @Test
    fun `testSnippet should return SUCCESS`() {
        val request =
            TestRequest(
                "snippetId",
                "userId",
                "1.0",
                emptyMap(),
                listOf(""),
                listOf("test test"),
            )
        val expected =
            TestResponse(
                listOf("test test"),
                TestResult.SUCCESS,
                "Test succeeded",
            )
        val snippet = "println(\"test test\");"
        every { assetServiceClient.getAsset("snippets", "snippetId") } returns snippet
        val response = testingJobController.testSnippet(request).body
        assertNotNull(response)
        assertEquals(expected.result, response.result)
        assertEquals(expected.actual, response.actual)
        assertEquals(expected.message, response.message)
    }

    @Test
    fun `testSnippet should ignore readInput prompts and consume inputs in order`() {
        val request =
            TestRequest(
                "snippetId",
                "userId",
                "1.1",
                emptyMap(),
                listOf("Lara"),
                listOf("Hola Lara"),
            )
        val snippet = "let name: string = readInput(\"Nombre?\");\nprintln(\"Hola \" + name);"
        every { assetServiceClient.getAsset("snippets", "snippetId") } returns snippet

        val response = testingJobController.testSnippet(request).body

        assertNotNull(response)
        assertEquals(TestResult.SUCCESS, response.result)
        assertEquals(listOf("Hola Lara"), response.actual)
    }

    @Test
    fun `testSnippet should return ERROR when snippet needs more inputs than provided`() {
        val request =
            TestRequest(
                "snippetId",
                "userId",
                "1.1",
                emptyMap(),
                emptyList(),
                listOf("Hola"),
            )
        val snippet = "let name: string = readInput(\"Nombre?\");\nprintln(\"Hola \" + name);"
        every { assetServiceClient.getAsset("snippets", "snippetId") } returns snippet

        val response = testingJobController.testSnippet(request).body

        assertNotNull(response)
        assertEquals(TestResult.ERROR, response.result)
    }

    @Test
    fun `testSnippet should return FAILED`() {
        val request =
            TestRequest(
                "snippetId",
                "userId",
                "1.0",
                emptyMap(),
                listOf(""),
                listOf("THIS WILL FAIL"),
            )
        val expected =
            TestResponse(
                listOf("test test"),
                TestResult.FAILED,
                "Test failed: expected [THIS WILL FAIL] but received [test test]",
            )
        val snippet = "println(\"test test\");"
        every { assetServiceClient.getAsset("snippets", any()) } returns snippet

        val response = testingJobController.testSnippet(request).body

        assertNotNull(response)
        assertEquals(expected.result, response.result)
        assertEquals(expected.actual, response.actual)
        assertEquals(expected.message, response.message)
    }

    @Test
    fun `testSnippet should return ERROR`() {
        val request =
            TestRequest(
                "snippetId",
                "userId",
                "1.0",
                emptyMap(),
                listOf(""),
                listOf("expected"),
            )
        val expected =
            TestResponse(
                emptyList(),
                TestResult.ERROR,
                "Error while executing test",
            )
        every { assetServiceClient.getAsset("snippets", any()) } returns null

        val response = testingJobController.testSnippet(request).body

        assertNotNull(response)
        assertEquals(expected.result, response.result)
        assertEquals(expected.actual, response.actual)
        assertEquals(expected.message, response.message)
    }
}
