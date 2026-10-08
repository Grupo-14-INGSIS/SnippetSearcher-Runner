package com.grupo14IngSis.snippetSearcherRunner.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class RestTemplateConfig {
    /**
     * Usa el HttpClient del JDK en vez de HttpURLConnection: este último no soporta el método PATCH
     * (que usamos contra App para actualizar el estado de los snippets) y lanza
     * "Invalid HTTP method: PATCH".
     */
    @Bean
    fun restTemplate(): RestTemplate {
        val httpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
        requestFactory.setReadTimeout(Duration.ofSeconds(120))
        return RestTemplate(requestFactory)
    }
}
