package com.grupo14IngSis.snippetSearcherRunner.consumer

import com.grupo14IngSis.snippetSearcherApp.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.plugins.RunnerPlugin
import com.grupo14IngSis.snippetSearcherRunner.plugins.TestPlugin
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
) {
  private val logger = LoggerFactory.getLogger(SnippetTaskConsumer::class.java)
  private val group = "runner-group"
  private val consumer = Consumer.from(group, "runner-1")

  private val plugins: Map<String, RunnerPlugin> = mapOf(
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
          val messages = redisTemplate.opsForStream<String, String>().read(
            consumer,
            StreamReadOptions.empty().count(10).block(Duration.ofMillis(2000)),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
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

  private fun processMessage(record: MapRecord<String, String, String>) {
    val task = record.value["task"]
    val snippetId = record.value["snippetId"]

    if (!(task == null || snippetId == null)) {
      if (task in plugins) {
        logger.info("Received task '$task' for snippet '$snippetId' - messageId ${record.id}")
        // Get snippet from asset-service
        val snippet: String? = assetServiceClient.getAsset("snippet", snippetId)
        // Perform task
        plugins[task]!!.run(snippet)
      }
    }

    // ACK del mensaje
    redisTemplate.opsForStream<String, String>().acknowledge(streamKey, group, record.id)
    logger.info("ACK sent for message ${record.id}")
    // Send notification to App
  }
}