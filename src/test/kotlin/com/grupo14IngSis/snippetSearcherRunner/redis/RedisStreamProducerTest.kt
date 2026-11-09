package com.grupo14IngSis.snippetSearcherRunner.redis

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class RedisStreamProducerTest {
    /** Subclase mínima para llamar a `emit`, que es protected. */
    private class TestProducer(
        redis: StringRedisTemplate,
    ) : RedisStreamProducer("snippet-job-results", redis, 10_000) {
        fun send(fields: Map<String, String>) = emit(fields)
    }

    @Test
    fun `emit hace XADD al stream con MAXLEN aproximado`() {
        val ops = mockk<StreamOperations<String, String, String>>()
        val redis = mockk<StringRedisTemplate>()
        every { redis.opsForStream<String, String>() } returns ops
        val record = slot<MapRecord<String, String, String>>()
        val options = slot<XAddOptions>()
        every { ops.add(capture(record), capture(options)) } returns RecordId.of("1-0")

        val id = TestProducer(redis).send(mapOf("snippetId" to "s1", "status" to "ok"))

        assertEquals(RecordId.of("1-0"), id)
        assertEquals("snippet-job-results", record.captured.stream)
        assertEquals(mapOf("snippetId" to "s1", "status" to "ok"), record.captured.value)
        assertEquals(10_000L, options.captured.maxlen)
        assertEquals(true, options.captured.isApproximateTrimming)
    }
}
