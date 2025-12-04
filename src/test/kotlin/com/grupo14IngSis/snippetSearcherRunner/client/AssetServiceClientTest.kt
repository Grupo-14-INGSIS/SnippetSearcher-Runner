package com.grupo14IngSis.snippetSearcherRunner.client

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

class AssetServiceClientTest {
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var assetServiceClient: AssetServiceClient
    private val bucketUrl = "http://localhost:8080"

    @BeforeEach
    fun setUp() {
        restTemplate = RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        assetServiceClient = AssetServiceClient(restTemplate, bucketUrl)
    }

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `getAsset should return content when asset exists`() {
        val container = "snippets"
        val key = "test-snippet"
        val expectedContent = "println('Hello World')"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Content-Type", MediaType.TEXT_PLAIN_VALUE))
            .andRespond(withSuccess(expectedContent, MediaType.TEXT_PLAIN))

        val result = assetServiceClient.getAsset(container, key)

        assertEquals(expectedContent, result)
    }

    @Test
    fun `getAsset should return null when asset not found`() {
        val container = "snippets"
        val key = "nonexistent-snippet"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val result = assetServiceClient.getAsset(container, key)

        assertNull(result)
    }

    @Test
    fun `postAsset should return 200 when upload succeeds`() {
        val container = "snippets"
        val key = "new-snippet"
        val content = "println('New Snippet')"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header("Content-Type", MediaType.TEXT_PLAIN_VALUE))
            .andExpect(content().string(content))
            .andRespond(withSuccess())

        val result = assetServiceClient.postAsset(container, key, content)

        assertEquals(200, result)
    }

    @Test
    fun `postAsset should return 500 when upload fails`() {
        val container = "snippets"
        val key = "failing-snippet"
        val content = "println('Fail')"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withServerError())

        val result = assetServiceClient.postAsset(container, key, content)

        assertEquals(500, result)
    }

    @Test
    fun `deleteAsset should return 200 when deletion succeeds`() {
        val container = "snippets"
        val key = "delete-snippet"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Content-Type", MediaType.TEXT_PLAIN_VALUE))
            .andRespond(withSuccess())

        val result = assetServiceClient.deleteAsset(container, key)

        assertEquals(200, result)
    }

    @Test
    fun `deleteAsset should return 404 when asset does not exist`() {
        val container = "snippets"
        val key = "nonexistent-snippet"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val result = assetServiceClient.deleteAsset(container, key)

        assertEquals(404, result)
    }

    @Test
    fun `deleteAsset should return 500 when deletion fails with server error`() {
        val container = "snippets"
        val key = "error-snippet"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withServerError())

        val result = assetServiceClient.deleteAsset(container, key)

        assertEquals(500, result)
    }

    @Test
    fun `getAsset should handle special characters in container and key`() {
        val container = "user-snippets"
        val key = "snippet_2024-01-01"
        val expectedContent = "special content"

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(expectedContent, MediaType.TEXT_PLAIN))

        val result = assetServiceClient.getAsset(container, key)

        assertEquals(expectedContent, result)
    }

    @Test
    fun `postAsset should handle empty content`() {
        val container = "snippets"
        val key = "empty-snippet"
        val content = ""

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(content))
            .andRespond(withSuccess())

        val result = assetServiceClient.postAsset(container, key, content)

        assertEquals(200, result)
    }

    @Test
    fun `postAsset should handle large content`() {
        val container = "snippets"
        val key = "large-snippet"
        val content = "a".repeat(10000)

        mockServer.expect(requestTo("$bucketUrl/v1/asset/$container/$key"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(content))
            .andRespond(withSuccess())

        val result = assetServiceClient.postAsset(container, key, content)

        assertEquals(200, result)
    }
}
