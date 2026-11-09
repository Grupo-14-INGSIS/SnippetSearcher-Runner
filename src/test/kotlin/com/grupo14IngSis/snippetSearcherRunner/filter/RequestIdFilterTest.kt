package com.grupo14IngSis.snippetSearcherRunner.filter

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestIdFilterTest {
    private val filter = RequestIdFilter()

    @Test
    fun `usa el X-Request-Id que llega, lo pone en el MDC y lo devuelve`() {
        val request = MockHttpServletRequest("GET", "/api/v1/snippets/1").apply { addHeader("X-Request-Id", "req-1") }
        val response = MockHttpServletResponse()
        var idDuranteElRequest: String? = null
        val chain = FilterChain { _, _ -> idDuranteElRequest = MDC.get("requestId") }

        filter.doFilter(request, response, chain)

        assertEquals("req-1", idDuranteElRequest)
        assertEquals("req-1", response.getHeader("X-Request-Id"))
        assertNull(MDC.get("requestId"), "el MDC se limpia al terminar el request")
    }

    @Test
    fun `genera un id si no llega ninguno`() {
        val response = MockHttpServletResponse()

        filter.doFilter(MockHttpServletRequest("PUT", "/api/v1/snippets/2"), response, FilterChain { _, _ -> })

        val id = response.getHeader("X-Request-Id")
        assertFalse(id.isNullOrBlank())
    }
}
