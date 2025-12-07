package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations

class SnippetTaskConsumerTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var assetServiceClient: AssetServiceClient
    private lateinit var appClient: AppClient
    private lateinit var formattingService: FormattingService
    private lateinit var lintingService: LintingService
    private lateinit var snippetTaskConsumer: SnippetTaskConsumer
    private lateinit var streamOperations: StreamOperations<String, String, String>

    private val streamKey = "test-stream"

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        assetServiceClient = mockk()
        appClient = mockk()
        formattingService = mockk()
        lintingService = mockk()
        streamOperations = mockk(relaxed = true)

        every { redisTemplate.opsForStream<String, String>() } returns streamOperations

        snippetTaskConsumer =
            SnippetTaskConsumer(
                redisTemplate,
                streamKey,
                assetServiceClient,
                formattingService,
                appClient,
            )
    }

    @Test
    fun `test processMessage with valid task`() {
        val snippetId = "snippet-123"
        val userId = "user-456"
        val language = "kotlin"
        val snippetContent = "fun main() { println(\"Hello\") }"
        val rules = mapOf("rule1" to "value1")

        val record =
            StreamRecords.string(
                mapOf(
                    "task" to "test",
                    "snippetId" to snippetId,
                    "userId" to userId,
                    "language" to language,
                ),
            ).withStreamKey(streamKey)

        every { assetServiceClient.getAsset("snippet", snippetId) } returns snippetContent
        every { formattingService.getRules(userId, language) } returns rules

        val testPlugin = spyk(TestPlugin())
        val consumerWithSpy =
            SnippetTaskConsumer(
                redisTemplate,
                streamKey,
                assetServiceClient,
                formattingService,
                appClient,
            )
        val pluginsField = consumerWithSpy::class.java.getDeclaredField("plugins")
        pluginsField.isAccessible = true
        pluginsField.set(consumerWithSpy, mapOf("test" to testPlugin))

        consumerWithSpy.processMessage(record)

        verify { assetServiceClient.getAsset("snippet", snippetId) }
        verify { formattingService.getRules(userId, language) }
        verify { testPlugin.run(snippetContent, rules) }
        verify { streamOperations.acknowledge(streamKey, "runner-group", record.id) }
    }

    @Test
    fun `test processMessage with invalid task`() {
        val record =
            StreamRecords.string(
                mapOf(
                    "task" to "invalid-task",
                    "snippetId" to "snippet-123",
                    "userId" to "user-456",
                    "language" to "kotlin",
                ),
            ).withStreamKey(streamKey)

        snippetTaskConsumer.processMessage(record)

        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
        verify { streamOperations.acknowledge(streamKey, "runner-group", record.id) }
    }

    @Test
    fun `test processMessage with missing fields`() {
        val record =
            StreamRecords.string(
                mapOf("task" to "test"),
            ).withStreamKey(streamKey)

        snippetTaskConsumer.processMessage(record)

        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
        verify { streamOperations.acknowledge(streamKey, "runner-group", record.id) }
    }
}
