package com.grupo14IngSis.snippetSearcherRunner.jobs

import com.grupo14IngSis.snippetSearcherRunner.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/** Publica en `snippet-job-results` el resultado de cada trabajo. El Runner no sabe quién lo lee. */
@Component
class SnippetJobResultProducer(
    @Value("\${redis.streams.results}") streamKey: String,
    @Value("\${redis.streams.max-length:10000}") maxLength: Long,
    redis: StringRedisTemplate,
) : RedisStreamProducer(streamKey, redis, maxLength) {
    fun publish(result: SnippetJobResult) {
        emit(result.toFields())
    }
}
