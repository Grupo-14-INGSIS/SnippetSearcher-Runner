package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AppClient
import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.service.inputprovider.ExecutionInputProvider
import runner.src.main.kotlin.Runner
import java.io.File.createTempFile
import java.util.concurrent.CancellationException
import kotlin.concurrent.thread

/**
 * Execution instance of a single PrintScript snippet. There is a `SnippetExecution` for each running execution.
 */
class SnippetExecution(
    private val snippetId: String,
    private val userId: String,
    private val version: String,
    private val environment: Map<String, String>,
    private val appClient: AppClient?,
    private val assetServiceClient: AssetServiceClient,
) {
    private val inputProvider = ExecutionInputProvider(environment)
    private lateinit var executionThread: Thread
    private val executionId = userId + snippetId

    private val outputList = mutableListOf<String>()
    private var status: ExecutionEventType? = null

    fun onOutput(
        line: String,
        status: ExecutionEventType,
    ) {
        try {
            appClient?.sendLine(snippetId, executionId, line, status)
        } catch (e: Exception) {
            println("Failed to send output: ${e.message}")
        }
    }

    /**
     * Start snippet execution on a separated thread
     */
    fun start(): Boolean {
        val runner = Runner()

        executionThread =
            thread(start = true) {
                val tempFile = createTempFile(snippetId, ".ps")
                try {
                    val snippet =
                        assetServiceClient.getAsset("snippets", snippetId)
                            ?: throw IllegalArgumentException("Snippet $snippetId not found")
                    tempFile.writeText(snippet)
                    tempFile.deleteOnExit()
                    val snippetPath = tempFile.absolutePath

                    runner.executionCommand(
                        listOf(snippetPath, version),
                        this.inputProvider,
                        printer = { output ->
                            outputList.add(output.toString())
                            onOutput(output.toString(), ExecutionEventType.OUTPUT)
                        },
                    )
                    onOutput("Execution finished.", ExecutionEventType.COMPLETED)
                    status = ExecutionEventType.COMPLETED
                } catch (e: CancellationException) {
                    onOutput("Execution canceled", ExecutionEventType.CANCELLED)
                    status = ExecutionEventType.CANCELLED
                } catch (e: IllegalArgumentException) {
                    onOutput("Error: ${e.message}", ExecutionEventType.ERROR)
                    status = ExecutionEventType.ERROR
                } catch (e: Exception) {
                    onOutput("Unexpected error: ${e.message}", ExecutionEventType.ERROR)
                    status = ExecutionEventType.ERROR
                } finally {
                    tempFile.delete()
                }
            }
        return true
    }

    fun getOutput(): List<String> {
        return outputList.toList()
    }

    fun getStatus(): ExecutionEventType? {
        return status
    }

    /**
     * Send or enqueue a single input.
     */
    fun sendInput(input: String) {
        inputProvider.enqueueInput(input)
    }

    /**
     * Send or enqueue multiple inputs.
     */
    fun sendMultipleInputs(inputs: List<String>) {
        for (input in inputs) {
            inputProvider.enqueueInput(input)
        }
    }

    /**
     * Cancel snippet execution.
     */
    fun cancel() {
        if (this::executionThread.isInitialized && executionThread.isAlive) {
            executionThread.interrupt()

            // Fallback: espera 5 segundos y fuerza stop
            Thread {
                Thread.sleep(5000)
                var attempts = 5
                while (executionThread.isAlive && attempts > 0) {
                    executionThread.interrupt() // Último recurso, es peligroso pero funcional
                    attempts--
                }
                if (!executionThread.isAlive) {
                    onOutput("Execution forcefully terminated", ExecutionEventType.CANCELLED)
                } else {
                    onOutput("Could not terminate execution", ExecutionEventType.ERROR)
                }
            }.apply { isDaemon = true }.start()
        }
    }

    fun isRunning(): Boolean {
        return if (this::executionThread.isInitialized) executionThread.isAlive else false
    }
}
