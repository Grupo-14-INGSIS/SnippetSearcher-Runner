package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations

class SnippetTaskConsumerTest {
    @RelaxedMockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @RelaxedMockK
    private lateinit var streamOperations: StreamOperations<String, String, String>

    @RelaxedMockK
    private lateinit var assetServiceClient: AssetServiceClient

    @RelaxedMockK
    private lateinit var appClient: AppClient

    @RelaxedMockK
    private lateinit var formattingService: FormattingService

    // Mocks for plugins that are in the map
    @RelaxedMockK
    private lateinit var formattingPlugin: FormattingPlugin

    @RelaxedMockK
    private lateinit var validationPlugin: ValidationPlugin

    @RelaxedMockK
    private lateinit var testPlugin: TestPlugin

    private lateinit var snippetTaskConsumer: SnippetTaskConsumer

    private val streamKey = "test-stream"
    private val group = "runner-group"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { redisTemplate.opsForStream<String, String>() } returns streamOperations

        snippetTaskConsumer =
            SnippetTaskConsumer(
                redisTemplate,
                streamKey,
                assetServiceClient,
                formattingService,
                appClient,
            )

        // Replace the real plugins with our mocks using reflection
        val pluginsField = snippetTaskConsumer::class.java.getDeclaredField("plugins")
        pluginsField.isAccessible = true
        pluginsField.set(
            snippetTaskConsumer,
            mapOf(
                "format" to formattingPlugin,
                "lint" to validationPlugin,
                "test" to testPlugin,
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun createRecord(
        task: String,
        snippetId: String = "s-1",
        userId: String = "u-1",
        language: String = "lang-1",
    ): MapRecord<String, String, String> {
        return MapRecord.create(streamKey, mapOf("task" to task, "snippetId" to snippetId, "userId" to userId, "language" to language))
            .withId(RecordId.of("1-0"))
    }

    @Test
    fun `processMessage with 'format' task executes formatting plugin`() {
        // Given
        val record = createRecord("format")
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { formattingPlugin.run(any(), any()) }
        verify(exactly = 0) { validationPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with 'lint' task executes validation plugin`() {
        // Given
        val record = createRecord("lint")
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { validationPlugin.run(any(), any()) }
        verify(exactly = 0) { formattingPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with 'test' task executes a new TestPlugin`() {
        // Given
        val record = createRecord("test", snippetId = "snippet-for-test")
        mockkConstructor(TestPlugin::class)
        every { anyConstructed<TestPlugin>().run(any(), any()) } returns "test output"

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify { anyConstructed<TestPlugin>().run("snippet-for-test", null) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
        // The mock in the map should not be called
        verify(exactly = 1) { testPlugin.run(any(), any()) }
    }

    @Test
    fun `processMessage updates snippet status before processing`() {
        // Given
        val record = createRecord("format", snippetId = "s-2", userId = "u-2")

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { appClient.updateSnippetTaskStatus("s-2", "u-2", "format", true) }
    }

    @Test
    fun `processMessage with unknown task does not run plugins`() {
        // Given
        val record = createRecord("unknown-task")

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { formattingPlugin.run(any(), any()) }
        verify(exactly = 0) { validationPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with missing fields does not run any logic`() {
        // Given
        val record = MapRecord.create(streamKey, mapOf("task" to "format")).withId(RecordId.of("3-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
        verify(exactly = 0) { formattingPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }
}
