package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.TestRequest
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResponse
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestingJobControllerTest {
    private val assetServiceClient: AssetServiceClient = mockk()
    private val testingJobController = TestingJobController(assetServiceClient)

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
