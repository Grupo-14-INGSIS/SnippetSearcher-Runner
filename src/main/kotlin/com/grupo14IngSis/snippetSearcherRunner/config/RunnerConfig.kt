package com.grupo14IngSis.snippetSearcherRunner.config

import org.example.Runner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RunnerConfig {
    @Bean
    fun runner(): Runner {
        return Runner()
    }
}
