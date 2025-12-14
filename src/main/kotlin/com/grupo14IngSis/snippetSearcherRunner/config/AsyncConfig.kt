package com.grupo14IngSis.snippetSearcherRunner.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Decorador de tareas que propaga el contexto MDC del hilo de la petición
 * a los hilos que ejecutan las tareas asíncronas.
 */
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        // Captura el contexto del hilo actual (el de la petición web)
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            try {
                // Establece el contexto capturado en el hilo de la tarea asíncrona
                MDC.setContextMap(contextMap)
                runnable.run()
            } finally {
                // Limpia el contexto del hilo de la tarea para evitar memory leaks
                MDC.clear()
            }
        }
    }
}

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

        // Aquí adjuntamos nuestro decorador de MDC
        executor.setTaskDecorator(MdcTaskDecorator())

        // Esperar a que terminen las tareas al cerrar la aplicación
        executor.setWaitForTasksToCompleteOnShutdown(true)

        // Tiempo de espera máximo (en segundos) al cerrar
        executor.setAwaitTerminationSeconds(60)

        executor.initialize()

        return executor
    }
}
