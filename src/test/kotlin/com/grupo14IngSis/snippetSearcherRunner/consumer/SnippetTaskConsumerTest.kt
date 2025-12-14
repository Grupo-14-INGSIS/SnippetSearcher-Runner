package com.grupo14IngSis.snippetSearcherRunner.consumer

class SnippetTaskConsumerTest {
    /*
    @RelaxedMockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @RelaxedMockK
    private lateinit var streamOperations: StreamOperations<String, String, String>

    @RelaxedMockK
    private lateinit var valueOperations: ValueOperations<String, String>

    @RelaxedMockK
    private lateinit var assetServiceClient: AssetServiceClient

    @RelaxedMockK
    private lateinit var appClient: AppClient

    @RelaxedMockK
    private lateinit var formattingService: FormattingService

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
        every { redisTemplate.opsForValue() } returns valueOperations

        snippetTaskConsumer =
            SnippetTaskConsumer(
                redisTemplate,
                streamKey,
                assetServiceClient,
                formattingService,
                appClient,
            )

        // Replace plugins with mocks
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
        return MapRecord.create(
            streamKey,
            mapOf(
                "task" to task,
                "snippetId" to snippetId,
                "userId" to userId,
                "language" to language,
            ),
        ).withId(RecordId.of("1-0"))
    }

    // ============== Tests de startConsuming() ==============

    @Test
    fun `startConsuming should read messages from stream`() {
        // Given
        val record = createRecord("format")
        every {
            streamOperations.read(any<Consumer>(), any<StreamReadOptions>(), any<StreamOffset<String>>())
        } returns listOf(record) andThen null

        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"
        every { formattingService.getRules(any(), any()) } returns emptyMap()

        // When - simulate one iteration of the loop
        val messages =
            streamOperations.read(
                Consumer.from(group, "runner-1"),
                StreamReadOptions.empty(),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            )

        if (messages != null) {
            for (msg in messages) {
                snippetTaskConsumer.processMessage(msg)
            }
        }

        // Then
        verify(atLeast = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `startConsuming should handle null messages gracefully`() {
        // Given
        every {
            streamOperations.read(any<Consumer>(), any<StreamReadOptions>(), any<StreamOffset<String>>())
        } returns null

        // When & Then - should not throw
        assertDoesNotThrow {
            val messages =
                streamOperations.read(
                    Consumer.from(group, "runner-1"),
                    StreamReadOptions.empty(),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                )
            // messages is null, so loop won't process anything
        }
    }

    @Test
    fun `startConsuming should handle exceptions during stream read`() {
        // Given
        every {
            streamOperations.read(any<Consumer>(), any<StreamReadOptions>(), any<StreamOffset<String>>())
        } throws RuntimeException("Connection lost")

        // When & Then - should not crash the consumer
        assertDoesNotThrow {
            try {
                streamOperations.read(
                    Consumer.from(group, "runner-1"),
                    StreamReadOptions.empty(),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                )
            } catch (e: Exception) {
                // Expected - consumer logs error and continues
            }
        }
    }

    // ============== Tests existentes mejorados ==============

    @Test
    fun `processMessage with format task executes formatting plugin`() {
        // Given
        val record = createRecord("format")
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"
        every { formattingService.getRules(any(), any()) } returns mapOf("rule1" to "value1")

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { appClient.updateSnippetTaskStatus("s-1", "u-1", "format", true) }
        verify(exactly = 1) { assetServiceClient.getAsset("snippet", "s-1") }
        verify(exactly = 1) { formattingService.getRules("u-1", "lang-1") }
        verify(exactly = 1) { formattingPlugin.run("snippet content", mapOf("rule1" to "value1")) }
        verify(exactly = 0) { validationPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with lint task executes validation plugin`() {
        // Given
        val record = createRecord("lint")
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"
        every { formattingService.getRules(any(), any()) } returns emptyMap()

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { validationPlugin.run("snippet content", emptyMap()) }
        verify(exactly = 0) { formattingPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with test task executes a new TestPlugin`() {
        // Given
        val record = createRecord("test", snippetId = "snippet-for-test")
        mockkConstructor(TestPlugin::class)
        every { anyConstructed<TestPlugin>().run(any(), any()) } returns "test output"
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet content"
        every { formattingService.getRules(any(), any()) } returns emptyMap()

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify { anyConstructed<TestPlugin>().run("snippet-for-test", null) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage updates snippet status before processing`() {
        // Given
        val record = createRecord("format", snippetId = "s-2", userId = "u-2")
        every { assetServiceClient.getAsset(any(), any()) } returns "content"

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
        verify(exactly = 1) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
        verify(exactly = 0) { formattingPlugin.run(any(), any()) }
        verify(exactly = 0) { validationPlugin.run(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with missing task field does not run any logic`() {
        // Given
        val record =
            MapRecord.create(
                streamKey,
                mapOf("snippetId" to "s-1", "userId" to "u-1", "language" to "lang-1"),
            ).withId(RecordId.of("3-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 0) { assetServiceClient.getAsset(any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with missing snippetId field does not run any logic`() {
        // Given
        val record =
            MapRecord.create(
                streamKey,
                mapOf("task" to "format", "userId" to "u-1", "language" to "lang-1"),
            ).withId(RecordId.of("4-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with missing userId field does not run any logic`() {
        // Given
        val record =
            MapRecord.create(
                streamKey,
                mapOf("task" to "format", "snippetId" to "s-1", "language" to "lang-1"),
            ).withId(RecordId.of("5-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with missing language field does not run any logic`() {
        // Given
        val record =
            MapRecord.create(
                streamKey,
                mapOf("task" to "format", "snippetId" to "s-1", "userId" to "u-1"),
            ).withId(RecordId.of("6-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 0) { appClient.updateSnippetTaskStatus(any(), any(), any(), any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with all null fields acknowledges message`() {
        // Given
        val record =
            MapRecord.create(
                streamKey,
                mapOf<String, String>(),
            ).withId(RecordId.of("7-0"))

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage with null snippet from assetService still acknowledges`() {
        // Given
        val record = createRecord("format")
        every { assetServiceClient.getAsset(any(), any()) } returns null

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { assetServiceClient.getAsset("snippet", "s-1") }
        verify(exactly = 1) { formattingPlugin.run(null, any()) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, record.id) }
    }

    @Test
    fun `processMessage handles different record IDs correctly`() {
        // Given
        val record1 = createRecord("format").withId(RecordId.of("10-0"))
        val record2 = createRecord("lint").withId(RecordId.of("20-5"))
        every { assetServiceClient.getAsset(any(), any()) } returns "content"

        // When
        snippetTaskConsumer.processMessage(record1)
        snippetTaskConsumer.processMessage(record2)

        // Then
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, RecordId.of("10-0")) }
        verify(exactly = 1) { streamOperations.acknowledge(streamKey, group, RecordId.of("20-5")) }
    }

    @Test
    fun `processMessage with empty rules map works correctly`() {
        // Given
        val record = createRecord("format")
        every { assetServiceClient.getAsset(any(), any()) } returns "snippet"
        every { formattingService.getRules(any(), any()) } returns emptyMap()

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { formattingPlugin.run("snippet", emptyMap()) }
    }

    @Test
    fun `processMessage with multiple rules works correctly`() {
        // Given
        val record = createRecord("lint")
        val rules = mapOf("rule1" to "val1", "rule2" to "val2", "rule3" to "val3")
        every { assetServiceClient.getAsset(any(), any()) } returns "code"
        every { formattingService.getRules(any(), any()) } returns rules

        // When
        snippetTaskConsumer.processMessage(record)

        // Then
        verify(exactly = 1) { validationPlugin.run("code", rules) }
    }
     */
}
