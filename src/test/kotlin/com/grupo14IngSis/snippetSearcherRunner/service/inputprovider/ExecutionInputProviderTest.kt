package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.service.inputprovider.ExecutionInputProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecutionInputProviderTest {
    @Test
    fun `Should return given input`() {
        val inputProvider = ExecutionInputProvider(emptyMap())
        val input = "Input"
        inputProvider.enqueueInput(input)
        assertEquals(input, inputProvider.readInput(""))
    }

    @Test
    fun `Should return given inputs in order`() {
        val inputProvider = ExecutionInputProvider(emptyMap())
        val input1 = "Hello, "
        val input2 = "World!"
        inputProvider.enqueueInput(input1)
        inputProvider.enqueueInput(input2)
        assertEquals(input1, inputProvider.readInput(""))
        assertEquals(input2, inputProvider.readInput(""))
    }

    @Test
    fun `Should return given input list in order`() {
        val inputProvider = ExecutionInputProvider(emptyMap())
        val input = "This is a very long massage!"
        for (char in input.chars()) {
            inputProvider.enqueueInput(char.toString())
        }
        for (char in input.chars()) {
            assertEquals(char.toString(), inputProvider.readInput(input))
        }
    }
}
