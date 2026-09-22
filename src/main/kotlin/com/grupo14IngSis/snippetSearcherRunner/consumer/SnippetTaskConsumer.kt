package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.AnalyzerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.FormattingPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
import com.grupo14IngSis.snippetSearcherRunner.service.FormattingService
import com.grupo14IngSis.snippetSearcherRunner.service.LintingService
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
    private val lintingService: LintingService,
    private val appClient: AppClient,
) {
    private val logger = LoggerFactory.getLogger(SnippetTaskConsumer::class.java)
    private val consumerGroup = "runner_group"
    private val consumerName = "runner_consumer_${UUID.randomUUID()}"

    @Volatile
    private var running = false
    private var consumerThread: Thread? = null

    private val formattingPlugin = FormattingPlugin()
    private val analyzerPlugin = AnalyzerPlugin()
    private val testPlugin = TestPlugin()

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
            logger.info("Consumer group '$consumerGroup' already exists: ${e.message}")
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

                            try {
                                processMessage(message)
                            } catch (e: Exception) {
                                logger.error("Unexpected error in processMessage for message ${message.id}", e)
                            } finally {
                                try {
                                    redisTemplate.opsForStream<String, String>()
                                        .acknowledge(streamKey, consumerGroup, message.id)
                                    logger.debug("ACK sent for message ${message.id}")
                                } catch (ackEx: Exception) {
                                    logger.warn("Could not send ACK for message ${message.id}: ${ackEx.message}")
                                }
                            }
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
        val rawTask = record.value["task"]
        val userId = record.value["userId"]
        val snippetId = record.value["snippetId"]
        val language = record.value["language"] ?: "printscript"

        if (rawTask == null || snippetId == null || userId == null) {
            logger.warn("Received incomplete record: ${record.value}")
            return
        }

        val task = rawTask.lowercase()
        logger.info("Received task '$task' for snippet '$snippetId' (user: '$userId') - messageId ${record.id}")

        when (task) {
            "format", "formatting" -> processFormattingTask(snippetId, userId, language)
            "lint", "linting" -> processLintingTask(snippetId, userId, language)
            "test", "testing" -> {
                try {
                    testPlugin.run(snippetId, null)
                    appClient.updateSnippetTaskStatus(snippetId, userId, "test", true)
                } catch (e: Exception) {
                    logger.error("Error executing test plugin for snippet '$snippetId': ${e.message}", e)
                    appClient.updateSnippetTaskStatus(snippetId, userId, "test", false)
                }
            }
            else -> {
                logger.warn("Unknown task '$task' for snippet '$snippetId'")
            }
        }
    }

    private fun processFormattingTask(snippetId: String, userId: String, language: String) {
        val snippet = assetServiceClient.getAsset("snippets", snippetId)
            ?: assetServiceClient.getAsset("snippet", snippetId)

        if (snippet == null) {
            logger.warn("Snippet '$snippetId' not found in asset service for formatting")
            appClient.updateSnippetTaskStatus(snippetId, userId, "formatting", false)
            return
        }

        try {
            val rules = formattingService.getRules(userId, language)
            val formatted = formattingPlugin.run(snippet, rules) as String

            // Save formatted content back to asset service (both containers for robustness)
            assetServiceClient.postAsset("snippets", snippetId, formatted)
            assetServiceClient.postAsset("snippet", snippetId, formatted)

            logger.info("Successfully formatted and saved snippet '$snippetId' for user '$userId'")
            appClient.updateSnippetTaskStatus(snippetId, userId, "formatting", true)
        } catch (e: Exception) {
            logger.error("Error during automatic formatting of snippet '$snippetId': ${e.message}", e)
            appClient.updateSnippetTaskStatus(snippetId, userId, "formatting", false)
        }
    }

    private fun processLintingTask(snippetId: String, userId: String, language: String) {
        val snippet = assetServiceClient.getAsset("snippets", snippetId)
            ?: assetServiceClient.getAsset("snippet", snippetId)

        if (snippet == null) {
            logger.warn("Snippet '$snippetId' not found in asset service for linting")
            appClient.updateSnippetTaskStatus(snippetId, userId, "linting", false, "failed")
            return
        }

        try {
            val rules = lintingService.getRules(userId, language)
            val output = analyzerPlugin.run(snippet, rules) as String

            val hasSyntaxError = output.contains("SYNTAX ERROR", ignoreCase = true) ||
                output.contains("Error:", ignoreCase = true)
            val hasLintIssues = output.contains("ANALYSIS RESULTS", ignoreCase = true) ||
                output.contains("issue(s) found", ignoreCase = true)

            val compliance = when {
                hasSyntaxError -> "failed"
                hasLintIssues -> "not-compliant"
                else -> "compliant"
            }
            val isSuccess = compliance == "compliant"

            logger.info("Successfully linted snippet '$snippetId' for user '$userId': compliance = $compliance")
            appClient.updateSnippetTaskStatus(snippetId, userId, "linting", isSuccess, compliance)
        } catch (e: Exception) {
            logger.error("Error during automatic linting of snippet '$snippetId': ${e.message}", e)
            appClient.updateSnippetTaskStatus(snippetId, userId, "linting", false, "failed")
        }
    }
}

