package com.grupo14IngSis.snippetSearcherRunner.filter

import com.newrelic.api.agent.NewRelic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestIdFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RequestIdFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // recibe el UUID en vez de generarlo
        val requestId = request.getHeader("X-Request-ID") ?: "unknown"

        MDC.put("request_id", requestId)
        response.setHeader("X-Request-ID", requestId)

        NewRelic.addCustomParameter("request_id", requestId)
        NewRelic.addCustomParameter("service", "Runner")

        logger.info("[RUNNER] Request $requestId - ${request.method} ${request.requestURI}")

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}