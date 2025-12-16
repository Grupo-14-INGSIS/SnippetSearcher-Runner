package com.grupo14IngSis.snippetSearcherRunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.grupo14IngSis.snippetSearcherRunner", "com.grupo14IngSis.snippetSearcherRunner.plugins"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
