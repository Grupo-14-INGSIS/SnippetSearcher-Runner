package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherRunner.service.inputprovider.ExecutionInputProvider
import com.grupo14IngSis.snippetSearcherRunner.storage.SnippetStorage
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
    private val storage: SnippetStorage,
    /** Se llama con cada línea apenas se produce (isPrompt = es el prompt de un readInput): output en vivo. */
    private val onLine: ((line: String, isPrompt: Boolean) -> Unit)? = null,
) {
    private val inputProvider = ExecutionInputProvider(environment) { prompt -> onPrompt(prompt) }
    private lateinit var executionThread: Thread

    private val outputList: MutableList<String> = Collections.synchronizedList(mutableListOf())

    /** Indices (en outputList) de las lineas que son prompts de readInput y no println. */
    private val promptIndices: MutableSet<Int> = Collections.synchronizedSet(mutableSetOf())

    @Volatile
    private var status: ExecutionEventType? = null

    fun onOutput(line: String) {
        outputList.add(line)
        notifyLine(line, false)
    }

    private fun onPrompt(prompt: String) {
        synchronized(outputList) {
            promptIndices.add(outputList.size)
            outputList.add(prompt)
        }
        notifyLine(prompt, true)
    }

    // Un fallo del listener (p. ej. Redis caído) no puede cortar la ejecución
    @Suppress("TooGenericExceptionCaught")
    private fun notifyLine(
        line: String,
        isPrompt: Boolean,
    ) {
        try {
            onLine?.invoke(line, isPrompt)
        } catch (_: Exception) {
        }
    }

    /**
     * Output producido solo por `println` (sin los prompts de `readInput`). Es lo que se compara
     * contra el output esperado de un test.
     */
    fun getPrintedOutput(): List<String> {
        synchronized(outputList) {
            return outputList.filterIndexed { index, _ -> index !in promptIndices }
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
                        storage.get(snippetId)
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
