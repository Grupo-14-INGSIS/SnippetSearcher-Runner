package com.grupo14IngSis.snippetSearcherRunner.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfig {
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        // Número de threads que siempre estarán activos
        executor.corePoolSize = 2

        // Número máximo de threads
        executor.maxPoolSize = 5

        // Capacidad de la cola de trabajos
        executor.queueCapacity = 100

        // Prefijo para los nombres de los threads
        executor.setThreadNamePrefix("formatting-async-")

        // Esperar a que terminen las tareas al cerrar la aplicación
        executor.setWaitForTasksToCompleteOnShutdown(true)

        // Tiempo de espera máximo (en segundos) al cerrar
        executor.setAwaitTerminationSeconds(60)

        executor.initialize()

        return executor
    }
}
