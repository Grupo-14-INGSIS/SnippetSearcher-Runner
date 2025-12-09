package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.RunnerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class SnippetTaskConsumer(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.key}") private val streamKey: String,
    private val assetServiceClient: AssetServiceClient,
    private val formattingService: FormattingService,
    private val appClient: AppClient,
) {
    private val logger = LoggerFactory.getLogger(SnippetTaskConsumer::class.java)
    private val group = "runner-group"
    private val consumer = Consumer.from(group, "runner-1")

    private val plugins: Map<String, RunnerPlugin> =
        mapOf(
            "format" to FormattingPlugin(),
            "lint" to ValidationPlugin(),
            "test" to TestPlugin(),
        )

    @PostConstruct
    fun init() {
        createConsumerGroupIfNeeded()
        startConsuming()
    }

    private fun createConsumerGroupIfNeeded() {
        try {
            redisTemplate.opsForValue().setIfAbsent(streamKey, "")
            redisTemplate.opsForStream<String, String>()
                .createGroup(streamKey, ReadOffset.latest(), group)
            logger.info("Consumer group '$group' created")
        } catch (ex: Exception) {
            logger.info("Consumer group '$group' already exists")
        }
    }

    private fun startConsuming() {
        Thread {
            logger.info("Runner listening for tasks on stream '$streamKey'...")
            while (true) {
                try {
                    val messages =
                        redisTemplate.opsForStream<String, String>().read(
                            consumer,
                            StreamReadOptions.empty().count(10).block(Duration.ofMillis(2000)),
                            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                        )
                    if (messages != null) {
                        for (msg in messages) {
                            processMessage(msg)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error while reading stream", e)
                }
            }
        }.start()
    }

    internal fun processMessage(record: MapRecord<String, String, String>) {
        val task = record.value["task"]
        val userId = record.value["userId"]
        val snippetId = record.value["snippetId"]
        val language = record.value["language"]

        if (!(task == null || snippetId == null || userId == null || language == null)) {
            appClient.updateSnippetTaskStatus(snippetId, userId, task, true)

            if (task == "test") {
                TestPlugin().run(snippetId, null)
            }
            if (task in plugins) {
                logger.info("Received task '$task' for snippet '$snippetId' - messageId ${record.id}")
                // Get snippet from asset-service
                val snippet: String? = assetServiceClient.getAsset("snippet", snippetId)
                // Get rules
                val rules = formattingService.getRules(userId, language)
                // Perform task
                plugins[task]!!.run(snippet, rules)
            }
        }

        // ACK del mensaje
        redisTemplate.opsForStream<String, String>().acknowledge(streamKey, group, record.id)
        logger.info("ACK sent for message ${record.id}")
    }
}
