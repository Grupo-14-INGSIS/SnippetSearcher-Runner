package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.service.inputprovider.ExecutionInputProvider
import runner.src.main.kotlin.Runner
import java.io.File.createTempFile
import java.util.Collections
import java.util.concurrent.CancellationException
import kotlin.concurrent.thread

/**
 * Execution instance of a single PrintScript snippet. There is a `SnippetExecution` for each running execution.
 */
class SnippetExecution(
    private val snippetId: String,
    private val version: String,
    private val environment: Map<String, String>,
    private val assetServiceClient: AssetServiceClient,
) {
    private val inputProvider = ExecutionInputProvider(environment) { prompt -> onOutput(prompt) }
    private lateinit var executionThread: Thread

    private val outputList: MutableList<String> = Collections.synchronizedList(mutableListOf())

    @Volatile
    private var status: ExecutionEventType? = null

    fun onOutput(line: String) {
        outputList.add(line)
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
                            onOutput(output.toString())
                        },
                    )

                    onOutput("Execution finished")
                    status = ExecutionEventType.COMPLETED
                } catch (e: CancellationException) {
                    onOutput("Execution canceled")
                    status = ExecutionEventType.CANCELLED
                } catch (e: IllegalArgumentException) {
                    onOutput("Error: ${e.message}")
                    status = ExecutionEventType.ERROR
                } catch (e: Exception) {
                    onOutput("Unexpected error: ${e.message}")
                    status = ExecutionEventType.ERROR
                } finally {
                    tempFile.delete()
                }
            }
        return true
    }

    fun getOutput(): List<String> {
        synchronized(outputList) {
            return outputList.toList()
        }
    }

    fun getStatus(): ExecutionEventType? = status

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
     * True while the snippet is blocked in `readInput` and no input has been provided yet.
     */
    fun isWaitingForInput(): Boolean = isRunning() && inputProvider.isWaitingForInput()

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
                    onOutput("Execution forcefully terminated")
                } else {
                    onOutput("Could not terminate execution")
                }
            }.apply { isDaemon = true }.start()
        }
    }

    fun isRunning(): Boolean = if (this::executionThread.isInitialized) executionThread.isAlive else false
}
