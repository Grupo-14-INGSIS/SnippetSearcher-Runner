package com.grupo14IngSis.snippetSearcherRunner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Bean
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        val factory = LettuceConnectionFactory(config)
        factory.afterPropertiesSet()
        return factory
    }

    // Un único template de Strings (claves, hashes y streams): es lo que usan el cache y los streams
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate = StringRedisTemplate(connectionFactory)
}
