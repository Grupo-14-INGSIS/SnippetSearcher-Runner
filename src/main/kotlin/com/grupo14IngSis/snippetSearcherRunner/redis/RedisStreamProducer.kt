package com.grupo14IngSis.snippetSearcherRunner.redis

import com.newrelic.api.agent.ConcurrentHashMapHeaders
import com.newrelic.api.agent.HeaderType
import com.newrelic.api.agent.NewRelic
import org.slf4j.MDC
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * Productor genérico de un Redis Stream (mismo rol que `RedisStreamProducer` de
 * austral-ingsis/class-redis-streams).
 *
 * Cada `emit` es un XADD con MAXLEN aproximado: el stream guarda historial (lo que permite
 * reintentos y consumidores nuevos), pero recortado para que no crezca sin límite en memoria.
 *
 * Cada mensaje lleva además el requestId y la traza de New Relic del pedido que lo generó (ver [traceFields]):
 * así el consumidor sigue el mismo pedido y sus logs quedan unidos a los del resto en New Relic.
 */
abstract class RedisStreamProducer(
    protected val streamKey: String,
    protected val redis: StringRedisTemplate,
    private val maxLength: Long,
) {
    protected fun emit(fields: Map<String, String>): RecordId? {
        val record = StreamRecords.newRecord().ofMap(fields + traceFields()).withStreamKey(streamKey)
        return redis
            .opsForStream<String, String>()
            .add(record, XAddOptions.maxlen(maxLength).approximateTrimming(true))
    }

    /**
     * El requestId (MDC) y los headers de traza distribuida de New Relic (`trace.newrelic`, `trace.traceparent`...).
     * Por HTTP el agente los pasa solo; por Redis hay que meterlos en el mensaje. Sin agente (tests), no agrega nada.
     */
    private fun traceFields(): Map<String, String> {
        val headers = ConcurrentHashMapHeaders.build(HeaderType.MESSAGE)
        NewRelic.getAgent().transaction.insertDistributedTraceHeaders(headers)
        val trace = headers.headerNames.mapNotNull { key -> headers.getHeader(key)?.let { "trace.$key" to it } }.toMap()
        val requestId = MDC.get("requestId")
        return if (requestId.isNullOrBlank()) trace else trace + ("requestId" to requestId)
    }
}
