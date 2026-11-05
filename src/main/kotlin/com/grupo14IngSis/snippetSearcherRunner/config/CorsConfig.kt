package com.grupo14IngSis.snippetSearcherRunner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * CORS del Runner: la UI lo llama directo desde el navegador (crear y editar snippets).
 *
 * Antes: allowedOriginPatterns("*") + allowCredentials(true), que hace que Spring devuelva como permitido el
 * Origin de cualquier sitio. Ahora solo los orígenes conocidos (lista separada por comas en
 * `app.cors.allowed-origins`) y sin credenciales: no se usan cookies. En las VMs la UI y el Runner comparten
 * dominio (/runner/) y CORS no interviene; hace falta para la UI local (localhost:5173 -> 19082).
 */
@Configuration
class CorsConfig(
    @Value("\${app.cors.allowed-origins:$DEFAULT_ALLOWED_ORIGINS}") private val allowedOrigins: List<String>,
) : WebMvcConfigurer {
    companion object {
        const val DEFAULT_ALLOWED_ORIGINS =
            "https://snippet26dev.duckdns.org,https://snippet26prod.duckdns.org,http://localhost:5173,http://127.0.0.1:5173"
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(*allowedOrigins.map { it.trim().trimEnd('/') }.filter { it.isNotEmpty() }.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            // Los headers que manda la UI (runnerService.ts)
            .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Request-Id")
            .allowCredentials(false)
            .maxAge(3600)
    }
}
