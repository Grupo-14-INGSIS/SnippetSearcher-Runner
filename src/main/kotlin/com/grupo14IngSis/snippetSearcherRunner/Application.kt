package com.grupo14IngSis.snippetSearcherRunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

// Scheduling: recuperación periódica de los mensajes del stream que quedaron sin confirmar
@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
