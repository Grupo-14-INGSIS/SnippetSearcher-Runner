package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class ExecutionProgressStoreTest {
    private val redis: StringRedisTemplate = mockk(relaxed = true)
    private val values: ValueOperations<String, String> = mockk(relaxed = true)
    private val lists: ListOperations<String, String> = mockk(relaxed = true)
    private val store = ExecutionProgressStore(redis)

    init {
        every { redis.opsForValue() } returns values
        every { redis.opsForList() } returns lists
    }

    @Test
    fun `key separates runs from test runs`() {
        assertEquals("execution:run:u1:s1", store.key(ExecutionProgressStore.RUN, "u1", "s1"))
        assertEquals("execution:test:u1:s1", store.key(ExecutionProgressStore.TEST, "u1", "s1"))
    }

    @Test
    fun `start clears the previous run and marks it running with a TTL`() {
        store.start("k")

        verify { redis.delete(listOf("k:output", "k:status")) }
        verify { values.set("k:status", "RUNNING", Duration.ofMinutes(10)) }
    }

    @Test
    fun `append pushes the line at the end`() {
        store.append("k", "hola")

        verify { lists.rightPush("k:output", "hola") }
    }

    @Test
    fun `a running execution is reported as OUTPUT with the lines so far`() {
        every { values.get("k:status") } returns "RUNNING"
        every { lists.range("k:output", 0, -1) } returns listOf("uno", "dos")

        val progress = store.get("k")

        assertEquals(ExecutionEventType.OUTPUT, progress?.status)
        assertEquals(listOf("uno", "dos"), progress?.message)
    }

    @Test
    fun `a finished execution keeps its final status`() {
        every { values.get("k:status") } returns "WAITING"
        every { lists.range("k:output", 0, -1) } returns listOf("name?")

        assertEquals(ExecutionEventType.WAITING, store.get("k")?.status)
    }

    @Test
    fun `no execution means null`() {
        every { values.get("k:status") } returns null

        assertNull(store.get("k"))
    }

    @Test
    fun `Redis down never breaks the execution`() {
        every { redis.opsForList() } throws RuntimeException("redis down")
        every { redis.opsForValue() } throws RuntimeException("redis down")

        assertDoesNotThrow { store.start("k") }
        assertDoesNotThrow { store.append("k", "x") }
        assertDoesNotThrow { store.finish("k", ExecutionEventType.COMPLETED) }
        assertNull(store.get("k"))
    }
}
