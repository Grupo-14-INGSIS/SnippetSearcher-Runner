package com.grupo14IngSis.snippetSearcherRunner.service.inputprovider

import inputprovider.src.main.kotlin.InputProvider
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CancellationException
import java.util.concurrent.LinkedBlockingQueue

/**
 * This `InputProvider` blocks the execution thread if there is no input available, waiting until an element becomes available.
 *
 * While blocked, `isWaitingForInput()` returns true so the execution can be reported as WAITING to the client.
 *
 * The method `cancelExecution breaks the execution thread safely`
 */
class ExecutionInputProvider(
    private val environment: Map<String, String>,
    private val onPrompt: ((String) -> Unit)? = null,
) : InputProvider {
    private val inputQueue: BlockingQueue<String> = LinkedBlockingQueue()

    @Volatile
    private var waitingForInput = false

    override fun readInput(prompt: String): String {
        try {
            if (prompt.isNotEmpty()) {
                onPrompt?.invoke(prompt)
            }
            if (inputQueue.isEmpty()) {
                waitingForInput = true
            }
            val input = inputQueue.take()
            waitingForInput = false
            return input
        } catch (e: Exception) {
            waitingForInput = false
            Thread.currentThread().interrupt()
            throw CancellationException("Execution cancelled while waiting for input")
        }
    }

    override fun readEnv(varName: String): String = environment[varName] ?: ""

    /**
     * Give input to execution
     */
    fun enqueueInput(input: String) {
        inputQueue.put(input)
    }

    fun isWaitingForInput(): Boolean = waitingForInput

    fun hasPendingInputs(): Boolean = inputQueue.isNotEmpty()
}
