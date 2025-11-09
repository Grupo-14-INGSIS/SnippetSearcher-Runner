package com.grupo14IngSis.snippetSearcher

import com.newrelic.api.agent.NewRelic
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RequestIdFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // Obtener o generar request-id
        val requestId = httpRequest.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()

        // Agregar a New Relic como custom attribute
        NewRelic.addCustomParameter("request_id", requestId)

        // Propagar en response
        httpResponse.setHeader("X-Request-Id", requestId)

        // Log
        println("[$requestId] ${httpRequest.method} ${httpRequest.requestURI}")

        chain.doFilter(request, response)
    }
}