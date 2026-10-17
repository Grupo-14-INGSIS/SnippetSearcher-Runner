package com.grupo14IngSis.snippetSearcherRunner.jobs

import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintOutcome
import com.grupo14IngSis.snippetSearcherRunner.plugins.LintReport
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorageException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.client.ResourceAccessException

class SnippetJobConsumerTest {
    private val storage: SnippetStorage = mockk()
    private val lintingService: LintingService = mockk()
    private val formattingService: FormattingService = mockk()
    private val resultProducer: SnippetJobResultProducer = mockk(relaxed = true)
    private val analyzer: AnalyzerPlugin = mockk()
    private val formatter: FormattingPlugin = mockk()

    private val consumer =
        SnippetJobConsumer(
            "snippet-jobs",
            "runner_group",
            mockk<StringRedisTemplate>(),
            storage,
            lintingService,
            formattingService,
            resultProducer,
            analyzer,
            formatter,
        )

    private val rules = mapOf<String, Any>("identifier_format" to true)

    private fun job(task: JobTask) = SnippetJob(task, "s1", "owner", "printscript", "1.1")

    @Test
    fun `lint uses the owner rules and the snippet version`() {
        every { storage.get("s1") } returns "let a: number = 1;"
        every { lintingService.getRules("owner", "printscript") } returns rules
        every { analyzer.lint("let a: number = 1;", rules + ("version" to "1.1")) } returns
            LintReport(LintOutcome.NOT_COMPLIANT, "ANALYSIS RESULTS: 1 issue(s) found")

        val result = consumer.handle(job(JobTask.LINT))

        assertEquals(JobOutcome.NOT_COMPLIANT, result.outcome)
        assertEquals("s1", result.snippetId)
    }

    @Test
    fun `compliant lint`() {
        every { storage.get("s1") } returns "code"
        every { lintingService.getRules(any(), any()) } returns rules
        every { analyzer.lint(any(), any()) } returns LintReport(LintOutcome.COMPLIANT, "SUCCESS")

        assertEquals(JobOutcome.COMPLIANT, consumer.handle(job(JobTask.LINT)).outcome)
    }

    @Test
    fun `code that cannot be analyzed is a permanent failure`() {
        every { storage.get("s1") } returns "let = ;"
        every { lintingService.getRules(any(), any()) } returns rules
        every { analyzer.lint(any(), any()) } returns LintReport(LintOutcome.INVALID, "SYNTAX ERROR")

        assertEquals(JobOutcome.FAILED, consumer.handle(job(JobTask.LINT)).outcome)
    }

    @Test
    fun `an exception from the analyzer is a permanent failure, not retried`() {
        // Caso real: el parser lanza "Statement must end with a semicolon..." para código roto
        every { storage.get("s1") } returns "let a: number = 1"
        every { lintingService.getRules(any(), any()) } returns rules
        every { analyzer.lint(any(), any()) } throws RuntimeException("Statement must end with a semicolon")

        val result = consumer.handle(job(JobTask.LINT))

        assertEquals(JobOutcome.FAILED, result.outcome)
        assertEquals("Statement must end with a semicolon", result.detail)
    }

    @Test
    fun `an exception from the formatter is a permanent failure and nothing is saved`() {
        every { storage.get("s1") } returns "let a: number = 1"
        every { formattingService.getRules(any(), any()) } returns rules
        every { formatter.run(any(), any()) } throws RuntimeException("parse error")

        assertEquals(JobOutcome.FAILED, consumer.handle(job(JobTask.FORMAT)).outcome)
        verify(exactly = 0) { storage.save(any(), any()) }
    }

    @Test
    fun `missing content is a permanent failure`() {
        every { storage.get("s1") } returns null

        val result = consumer.handle(job(JobTask.LINT))

        assertEquals(JobOutcome.FAILED, result.outcome)
        assertEquals("snippet content not found", result.detail)
    }

    @Test
    fun `user without rules is a permanent failure`() {
        every { storage.get("s1") } returns "code"
        every { lintingService.getRules(any(), any()) } returns emptyMap()

        assertEquals(JobOutcome.FAILED, consumer.handle(job(JobTask.LINT)).outcome)
    }

    @Test
    fun `bucket down is transient - the exception propagates so the message is retried`() {
        every { storage.get("s1") } throws ResourceAccessException("connection refused")

        assertThrows<ResourceAccessException> { consumer.handle(job(JobTask.LINT)) }
    }

    @Test
    fun `format saves the formatted content in the bucket`() {
        every { storage.get("s1") } returns "let a:number=1;"
        every { formattingService.getRules("owner", "printscript") } returns rules
        every { formatter.run("let a:number=1;", rules + ("version" to "1.1")) } returns "let a: number = 1;"
        every { storage.save("s1", "let a: number = 1;") } just runs

        val result = consumer.handle(job(JobTask.FORMAT))

        assertEquals(JobOutcome.FORMATTED, result.outcome)
        verify { storage.save("s1", "let a: number = 1;") }
    }

    @Test
    fun `format whose content cannot be saved is retried`() {
        every { storage.get("s1") } returns "code"
        every { formattingService.getRules(any(), any()) } returns rules
        every { formatter.run(any(), any()) } returns "formatted"
        every { storage.save(any(), any()) } throws SnippetStorageException("500")

        assertThrows<SnippetStorageException> { consumer.handle(job(JobTask.FORMAT)) }
    }

    @Test
    fun `onMessage publishes the result`() {
        every { storage.get("s1") } returns null

        consumer.onMessage(MapRecord.create("snippet-jobs", job(JobTask.LINT).toFieldsForTest()))

        verify { resultProducer.publish(SnippetJobResult(JobTask.LINT, "s1", JobOutcome.FAILED, "snippet content not found")) }
    }

    @Test
    fun `malformed message is discarded without publishing`() {
        consumer.onMessage(MapRecord.create("snippet-jobs", mapOf("task" to "lint")))

        verify(exactly = 0) { resultProducer.publish(any()) }
    }

    @Test
    fun `dead letter publishes a failed result`() {
        consumer.onDeadLetter(MapRecord.create("snippet-jobs", job(JobTask.FORMAT).toFieldsForTest()))

        verify { resultProducer.publish(SnippetJobResult(JobTask.FORMAT, "s1", JobOutcome.FAILED, "retries exhausted")) }
    }

    private fun SnippetJob.toFieldsForTest() =
        mapOf("task" to task.wire, "snippetId" to snippetId, "userId" to userId, "language" to language, "version" to version)
}
