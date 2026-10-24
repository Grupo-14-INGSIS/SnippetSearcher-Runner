package com.grupo14IngSis.snippetSearcherRunner.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.servlet.config.annotation.CorsRegistry

class CorsConfigTest {
    // getCorsConfigurations() es protected: se expone para el test
    private class TestRegistry : CorsRegistry() {
        fun configs(): Map<String, CorsConfiguration> = corsConfigurations
    }

    private fun corsFor(origins: List<String>): CorsConfiguration {
        val registry = TestRegistry()
        CorsConfig(origins).addCorsMappings(registry)
        return registry.configs().getValue("/**")
    }

    private val defaults = CorsConfig.DEFAULT_ALLOWED_ORIGINS.split(",")

    @Test
    fun `the UI domains and localhost are allowed`() {
        val cors = corsFor(defaults)

        assertEquals("https://snippet26dev.duckdns.org", cors.checkOrigin("https://snippet26dev.duckdns.org"))
        assertEquals("https://snippet26prod.duckdns.org", cors.checkOrigin("https://snippet26prod.duckdns.org"))
        assertEquals("http://localhost:5173", cors.checkOrigin("http://localhost:5173"))
    }

    @Test
    fun `any other site is rejected (antes con * se aceptaba cualquiera)`() {
        val cors = corsFor(defaults)

        assertNull(cors.checkOrigin("https://evil.example.com"))
        assertNull(cors.checkOrigin("http://localhost:3000"))
    }

    @Test
    fun `no credentials and no wildcard`() {
        val cors = corsFor(defaults)

        assertFalse(cors.allowCredentials ?: false)
        assertFalse(cors.allowedOrigins!!.contains("*"))
        assertNull(cors.allowedOriginPatterns)
    }

    @Test
    fun `origins from the property are trimmed and a trailing slash is ignored`() {
        val cors = corsFor(listOf(" https://a.example.com/ ", "", "http://localhost:5173"))

        assertEquals(listOf("https://a.example.com", "http://localhost:5173"), cors.allowedOrigins)
    }
}
