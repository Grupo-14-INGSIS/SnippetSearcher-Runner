package com.grupo14IngSis.snippetSearcherRunner.service.inputprovider

import inputprovider.src.main.kotlin.InputProvider
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CancellationException
import java.util.concurrent.LinkedBlockingQueue

/**
 * This `InputProvider` blocks the execution thread if there is no input available, waiting until an element becomes available.
 *
 * The method `cancelExecution breaks the execution thread safely`
 */
class ExecutionInputProvider(
    private val environment: Map<String, String>,
) : InputProvider {
    private val inputQueue: BlockingQueue<String> = LinkedBlockingQueue()

    override fun readInput(prompt: String): String {
        try {
            print(prompt)
            return inputQueue.take()
        } catch (e: Exception) {
            Thread.currentThread().interrupt()
            throw CancellationException("Execution cancelled while waiting for input")
        }
    }

    override fun readEnv(varName: String): String {
        return environment[varName] ?: ""
    }

    /**
     * Give input to execution
     */
    fun enqueueInput(input: String) {
        inputQueue.put(input)
    }
}
