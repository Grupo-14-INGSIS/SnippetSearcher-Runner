package com.grupo14IngSis.snippetSearcherRunner.jobs

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintOutcome
import com.grupo14IngSis.snippetSearcherRunner.redis.RedisStreamConsumer
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Consume `snippet-jobs` dentro del consumer group `runner_group`: con N réplicas del Runner,
 * Redis le entrega cada mensaje a una sola. Por cada trabajo publica un resultado en
 * `snippet-job-results`.
 *
 * Dos tipos de error, tratados distinto:
 *   - Permanentes (el snippet no existe, el código no parsea, el usuario no tiene reglas,
 *     cualquier error del analizador o del formateador): reintentar no cambia nada, así que se publica "failed" y el mensaje se confirma.
 *   - Transitorios (asset service o base caídos): se deja propagar la excepción, el mensaje queda
 *     pendiente y [RedisStreamConsumer] lo reintenta; si agota los intentos, [onDeadLetter]
 *     publica "failed".
 */
@Component
@Profile("!test")
class SnippetJobConsumer(
    @Value("\${redis.streams.jobs}") streamKey: String,
    @Value("\${redis.streams.jobs-group:runner_group}") groupId: String,
    redis: StringRedisTemplate,
    private val storage: SnippetStorage,
    private val lintingService: LintingService,
    private val formattingService: FormattingService,
    private val resultProducer: SnippetJobResultProducer,
    private val analyzerPlugin: AnalyzerPlugin,
    private val formattingPlugin: FormattingPlugin,
) : RedisStreamConsumer(streamKey, groupId, redis) {
    private val logger = LoggerFactory.getLogger(SnippetJobConsumer::class.java)

    public override fun onMessage(record: MapRecord<String, String, String>) {
        val job = SnippetJob.fromFields(record.value)
        if (job == null) {
            // Un mensaje mal formado nunca va a poder procesarse: se descarta (y se confirma)
            logger.warn("Discarding malformed job ${record.id}: ${record.value}")
            return
        }
        logger.info("Processing ${job.task.wire} job for snippet ${job.snippetId} (message ${record.id})")
        resultProducer.publish(handle(job))
    }

    public override fun onDeadLetter(record: MapRecord<String, String, String>) {
        val job = SnippetJob.fromFields(record.value) ?: return
        resultProducer.publish(SnippetJobResult(job.task, job.snippetId, JobOutcome.FAILED, "retries exhausted"))
    }

    internal fun handle(job: SnippetJob): SnippetJobResult {
        val content =
            storage.get(job.snippetId)
                ?: return failed(job, "snippet content not found")
        return when (job.task) {
            JobTask.LINT -> lint(job, content)
            JobTask.FORMAT -> format(job, content)
        }
    }

    private fun lint(
        job: SnippetJob,
        content: String,
    ): SnippetJobResult {
        val rules = lintingService.getRules(job.userId, job.language)
        if (rules.isEmpty()) return failed(job, "user has no linting rules")
        // El analizador no hace I/O: si lanza (código que no parsea, configuración inválida),
        // reintentar da el mismo error, así que es un fallo permanente.
        @Suppress("TooGenericExceptionCaught")
        val report =
            try {
                analyzerPlugin.lint(content, rules + ("version" to job.version))
            } catch (e: Exception) {
                return failed(job, e.message ?: "could not lint snippet")
            }
        val outcome =
            when (report.outcome) {
                LintOutcome.COMPLIANT -> JobOutcome.COMPLIANT
                LintOutcome.NOT_COMPLIANT -> JobOutcome.NOT_COMPLIANT
                LintOutcome.INVALID -> JobOutcome.FAILED
            }
        return SnippetJobResult(JobTask.LINT, job.snippetId, outcome, report.output.take(500))
    }

    private fun format(
        job: SnippetJob,
        content: String,
    ): SnippetJobResult {
        val rules = formattingService.getRules(job.userId, job.language)
        if (rules.isEmpty()) return failed(job, "user has no formatting rules")
        // Igual que el linteo: un error del formateador es permanente. El guardado (I/O) va afuera.
        @Suppress("TooGenericExceptionCaught")
        val formatted =
            try {
                formattingPlugin.run(content, rules + ("version" to job.version)) as String
            } catch (e: Exception) {
                return failed(job, e.message ?: "could not format snippet")
            }
        // Si el asset service no guarda, la excepción deja el mensaje pendiente para reintentar
        storage.save(job.snippetId, formatted)
        return SnippetJobResult(JobTask.FORMAT, job.snippetId, JobOutcome.FORMATTED)
    }

    private fun failed(
        job: SnippetJob,
        reason: String,
    ): SnippetJobResult {
        logger.warn("${job.task.wire} job for snippet ${job.snippetId} failed: $reason")
        return SnippetJobResult(job.task, job.snippetId, JobOutcome.FAILED, reason)
    }
}
