package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Progreso de una ejecución en Redis, para ver el output **a medida que se imprime** (US #9 y #10).
 *
 * Mientras el `POST` de la ejecución está en curso, cada línea que imprime el snippet se agrega a una
 * lista de Redis y la UI la consulta con `GET .../executions/status`. Vive en Redis y no en memoria
 * porque el Runner tiene varias réplicas: el `GET` puede caer en una réplica distinta de la que ejecuta.
 *
 * Claves (con TTL, se borran solas):
 *   execution:{kind}:{userId}:{snippetId}:status  -> RUNNING | WAITING | COMPLETED | ERROR | CANCELLED
 *   execution:{kind}:{userId}:{snippetId}:output  -> lista de líneas, en orden
 * `kind` es `run` (ejecución interactiva) o `test` (corrida de un test): no se pisan entre sí.
 *
 * Redis es solo para mostrar el progreso: si falla, la ejecución sigue y el resultado final llega
 * igual en la respuesta del `POST`.
 */
@Component
@Suppress("TooGenericExceptionCaught")
class ExecutionProgressStore(
    private val redis: StringRedisTemplate,
) {
    private val logger = LoggerFactory.getLogger(ExecutionProgressStore::class.java)

    companion object {
        const val RUN = "run"
        const val TEST = "test"
        private const val RUNNING = "RUNNING"
        private val TTL: Duration = Duration.ofMinutes(10)
    }

    fun key(
        kind: String,
        userId: String,
        snippetId: String,
    ): String = "execution:$kind:$userId:$snippetId"

    /** Arranca una ejecución nueva: borra el progreso de la anterior. */
    fun start(key: String) =
        quietly {
            redis.delete(listOf("$key:output", "$key:status"))
            redis.opsForValue().set("$key:status", RUNNING, TTL)
        }

    fun append(
        key: String,
        line: String,
    ) = quietly {
        redis.opsForList().rightPush("$key:output", line)
        redis.expire("$key:output", TTL)
    }

    fun finish(
        key: String,
        status: ExecutionEventType,
    ) = quietly { redis.opsForValue().set("$key:status", status.name, TTL) }

    /** Progreso actual, o null si no hay ninguna ejecución registrada para esa clave. */
    fun get(key: String): ExecutionResponse? =
        try {
            val status = redis.opsForValue().get("$key:status")
            if (status == null) {
                null
            } else {
                val lines = redis.opsForList().range("$key:output", 0, -1) ?: emptyList()
                val type = if (status == RUNNING) ExecutionEventType.OUTPUT else ExecutionEventType.valueOf(status)
                ExecutionResponse(type, lines)
            }
        } catch (e: Exception) {
            logger.warn("Could not read execution progress $key: ${e.message}")
            null
        }

    private fun quietly(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.warn("Could not update execution progress: ${e.message}")
        }
    }
}
