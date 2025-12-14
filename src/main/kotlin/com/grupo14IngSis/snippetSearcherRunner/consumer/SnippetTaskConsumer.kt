package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.RunnerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.ValidationPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

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
    private val consumerGroup = "runner_group"
    private val consumerName = "runner_consumer_${UUID.randomUUID()}"

    @Volatile
    private var running = false
    private var consumerThread: Thread? = null

    private val plugins: Map<String, RunnerPlugin> =
        mapOf(
            "format" to FormattingPlugin(),
            "lint" to ValidationPlugin(),
            "test" to TestPlugin(),
        )

    @PostConstruct
    fun init() {
        try {
            val type = redisTemplate.type(streamKey)
            if (type != DataType.STREAM) {
                logger.error("Key '$streamKey' has wrong type: $type. Deleting...")
                redisTemplate.delete(streamKey)
            }
            redisTemplate.opsForStream<String, String>()
                .createGroup(streamKey, ReadOffset.from("0"), consumerGroup)
            logger.info("Consumer group '$consumerGroup' created for stream '$streamKey'")
        } catch (e: RedisSystemException) {
            if (e.message?.contains("BUSYGROUP") == true) {
                logger.info("Consumer group '$consumerGroup' already exists")
            } else {
                throw e
            }
        }

        startConsuming()
    }

    private fun startConsuming() {
        running = true

        consumerThread =
            Thread {
                logger.info("Consumer started: $consumerName")
                while (running) {
                    try {
                        val messages =
                            redisTemplate.opsForStream<String, String>().read(
                                Consumer.from(consumerGroup, consumerName),
                                StreamReadOptions.empty()
                                    .count(1)
                                    .block(Duration.ofSeconds(2)),
                                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                            )
                        messages?.forEach { message ->
                            if (!running) return@forEach

                            processMessage(message)

                            // ACK del mensaje
                            redisTemplate.opsForStream<String, String>()
                                .acknowledge(streamKey, consumerGroup, message.id)
                        }
                    } catch (e: IllegalStateException) {
                        if (e.message?.contains("was destroyed") == true) {
                            logger.warn("Redis connection closed, stopping consumer")
                            break
                        } else {
                            throw e
                        }
                    } catch (e: InterruptedException) {
                        logger.info("Consumer interrupted, stopping...")
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        logger.error("Error while reading stream", e)
                        if (running) {
                            Thread.sleep(5000)
                        }
                    }
                }

                logger.info("Consumer stopped: $consumerName")
            }.apply {
                name = "Redis-Consumer-Thread"
                isDaemon = false
                start()
            }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down consumer...")
        running = false

        consumerThread?.let { thread ->
            thread.interrupt()

            try {
                thread.join(5000)
                if (thread.isAlive) {
                    logger.warn("Consumer thread did not stop gracefully")
                } else {
                    logger.info("Consumer thread stopped successfully")
                }
            } catch (e: InterruptedException) {
                logger.error("Interrupted while waiting for consumer to stop")
                Thread.currentThread().interrupt()
            }
        }
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
                val snippet: String? = assetServiceClient.getAsset("snippet", snippetId)
                val rules = formattingService.getRules(userId, language)
                plugins[task]!!.run(snippet, rules)
            }
        }

        // ACK del mensaje
        redisTemplate.opsForStream<String, String>().acknowledge(streamKey, group, record.id)
        logger.info("ACK sent for message ${record.id}")
    }
}
