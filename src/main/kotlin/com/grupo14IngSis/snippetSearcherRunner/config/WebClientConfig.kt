package com.grupo14IngSis.snippetSearcherRunner.config

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {
    /**
     * Configuración del WebClient para comunicación HTTP con otros servicios
     */
    @Bean
    fun webClient(): WebClient {
        // Configurar timeouts
        val httpClient =
            HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    conn.addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
                }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { configurer ->
                // Aumentar el límite de memoria para respuestas grandes
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB
            }
            .build()
    }
}
