package com.grupo14IngSis.snippetSearcherRunner.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class RestTemplateConfigTest {
    private val restTemplateConfig = RestTemplateConfig()

    @Test
    fun `restTemplate should create a non-null instance`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertNotNull(restTemplate)
    }

    @Test
    fun `restTemplate should be of type RestTemplate`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertTrue(restTemplate is RestTemplate)
    }

    @Test
    fun `restTemplate should have default message converters`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertNotNull(restTemplate.messageConverters)
        assertTrue(restTemplate.messageConverters.isNotEmpty())
    }

    @Test
    fun `restTemplate should have a request factory`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertNotNull(restTemplate.requestFactory)
        assertTrue(restTemplate.requestFactory is ClientHttpRequestFactory)
    }

    @Test
    fun `restTemplate should be capable of making GET requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("test response", org.springframework.http.MediaType.TEXT_PLAIN))

        val response = restTemplate.getForObject(url, String::class.java)

        assertEquals("test response", response)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should be capable of making POST requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"
        val requestBody = "test data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("created", org.springframework.http.MediaType.TEXT_PLAIN))

        val response = restTemplate.postForObject(url, requestBody, String::class.java)

        assertEquals("created", response)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should be capable of making PUT requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data/1"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess())

        assertDoesNotThrow {
            restTemplate.put(url, "updated data")
        }

        mockServer.verify()
    }

    @Test
    fun `restTemplate should be capable of making DELETE requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data/1"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withSuccess())

        assertDoesNotThrow {
            restTemplate.delete(url)
        }

        mockServer.verify()
    }

    @Test
    fun `restTemplate should handle different response types`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/number"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("42", org.springframework.http.MediaType.TEXT_PLAIN))

        val response = restTemplate.getForObject(url, String::class.java)

        assertEquals("42", response)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should support exchange method`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("exchange response", org.springframework.http.MediaType.TEXT_PLAIN))

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("exchange response", response.body)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should have error handler configured`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertNotNull(restTemplate.errorHandler)
    }

    @Test
    fun `restTemplate should have interceptors list available`() {
        val restTemplate = restTemplateConfig.restTemplate()

        assertNotNull(restTemplate.interceptors)
    }

    @Test
    fun `multiple restTemplate instances should be independent`() {
        val restTemplate1 = restTemplateConfig.restTemplate()
        val restTemplate2 = restTemplateConfig.restTemplate()

        assertNotSame(restTemplate1, restTemplate2)
    }

    @Test
    fun `restTemplate should support URI variables in requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/users/{id}"
        val userId = "123"

        mockServer.expect(requestTo("http://test.com/api/users/123"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("user data", org.springframework.http.MediaType.TEXT_PLAIN))

        val response = restTemplate.getForObject(url, String::class.java, userId)

        assertEquals("user data", response)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should handle JSON responses`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/json"
        val jsonResponse = """{"name":"test","value":123}"""

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(jsonResponse, org.springframework.http.MediaType.APPLICATION_JSON))

        val response = restTemplate.getForObject(url, String::class.java)

        assertEquals(jsonResponse, response)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should support custom headers in requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("response", org.springframework.http.MediaType.TEXT_PLAIN))

        val headers = org.springframework.http.HttpHeaders()
        headers.add("Custom-Header", "test-value")
        val entity = org.springframework.http.HttpEntity<Void>(headers)

        val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)

        assertEquals("response", response.body)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should be ready for immediate use`() {
        val restTemplate = restTemplateConfig.restTemplate()

        // Verificar que está completamente inicializado
        assertNotNull(restTemplate.requestFactory)
        assertNotNull(restTemplate.messageConverters)
        assertNotNull(restTemplate.errorHandler)
        assertTrue(restTemplate.messageConverters.size > 0)
    }

    @Test
    fun `restTemplate should support HEAD requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.HEAD))
            .andRespond(withSuccess())

        val headers = restTemplate.headForHeaders(url)

        assertNotNull(headers)
        mockServer.verify()
    }

    @Test
    fun `restTemplate should support OPTIONS requests`() {
        val restTemplate = restTemplateConfig.restTemplate()
        val mockServer = MockRestServiceServer.createServer(restTemplate)
        val url = "http://test.com/api/data"

        mockServer.expect(requestTo(url))
            .andExpect(method(HttpMethod.OPTIONS))
            .andRespond(withSuccess())

        val allowedMethods = restTemplate.optionsForAllow(url)

        assertNotNull(allowedMethods)
        mockServer.verify()
    }
}
