package com.grupo14IngSis.snippetSearcherRunner.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate

class SnippetCacheServiceTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var snippetCacheService: SnippetCacheService

    @BeforeEach
    fun setup() {
        redisTemplate = mockk(relaxed = true)
        snippetCacheService = SnippetCacheService(redisTemplate)
    }

    @Test
    fun `test get from cache hit`() {
        val key = "testKey"
        val value = "testValue"
        every { redisTemplate.opsForValue().get(key) } returns value

        val result = snippetCacheService.getFromCache(key)

        assertEquals(value, result)
    }

    @Test
    fun `test get from cache miss`() {
        val key = "testKey"
        every { redisTemplate.opsForValue().get(key) } returns null

        val result = snippetCacheService.getFromCache(key)

        assertEquals(null, result)
    }

    @Test
    fun `test save to cache`() {
        val key = "testKey"
        val value = "testValue"
        every { redisTemplate.opsForValue().set(any(), any()) } returns Unit

        snippetCacheService.saveToCache(key, value)

        verify { redisTemplate.opsForValue().set(key, value) }
    }

    @Test
    fun `test delete from cache success`() {
        val key = "testKey"
        every { redisTemplate.delete(key) } returns true

        snippetCacheService.deleteFromCache(key)

        verify { redisTemplate.delete(key) }
    }

    @Test
    fun `test delete from cache failure`() {
        val key = "testKey"
        every { redisTemplate.delete(key) } returns false

        snippetCacheService.deleteFromCache(key)

        verify { redisTemplate.delete(key) }
    }

    @Test
    fun `test get from cache exception`() {
        val key = "testKey"
        every { redisTemplate.opsForValue().get(key) } throws RuntimeException("Redis down")

        val result = snippetCacheService.getFromCache(key)

        assertEquals(null, result)
    }

    @Test
    fun `test save to cache exception`() {
        val key = "testKey"
        val value = "testValue"
        every { redisTemplate.opsForValue().set(any(), any(), any(), any()) } throws RuntimeException("Redis down")

        snippetCacheService.saveToCache(key, value)
    }

    @Test
    fun `test delete from cache exception`() {
        val key = "testKey"
        every { redisTemplate.delete(key) } throws RuntimeException("Redis down")

        snippetCacheService.deleteFromCache(key)

        verify { redisTemplate.delete(key) }
    }
}
