package com.grupo14IngSis.snippetSearcherRunner.service

import com.grupo14IngSis.snippetSearcherRunner.dto.ExecutionStep
import com.grupo14IngSis.snippetSearcherRunner.dto.StepType
import com.grupo14IngSis.snippetSearcherRunner.dto.TestResultDto
import com.grupo14IngSis.snippetSearcherRunner.model.Snippet
import com.grupo14IngSis.snippetSearcherRunner.model.TestCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TestExecutionService {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun executeTest(
        snippet: Snippet,
        test: TestCase,
    ): TestResultDto {
        logger.info("Executing test '${test.name}' for snippet ${snippet.id}")

        val executionSteps = mutableListOf<ExecutionStep>()
        val actualOutputs = mutableListOf<String>()

        try {
            var stepNumber = 1

            // Agregar pasos de input
            test.inputs.forEach { input ->
                executionSteps.add(
                    ExecutionStep(
                        stepNumber = stepNumber++,
                        type = StepType.INPUT,
                        content = "Input: $input",
                    ),
                )
            }

            // Ejecutar el snippet (simulación)
            val outputs = executeSnippetCode(snippet.code, test.inputs)

            // Procesar outputs
            outputs.forEach { output ->
                actualOutputs.add(output)
                executionSteps.add(
                    ExecutionStep(
                        stepNumber = stepNumber++,
                        type = StepType.OUTPUT,
                        content = "Output: $output",
                    ),
                )
            }

            // Comparar resultados
            val passed = compareOutputs(actualOutputs, test.expectedOutputs)

            logger.info("Test '${test.name}' ${if (passed) "PASSED" else "FAILED"}")

            return TestResultDto(
                testId = test.id,
                testName = test.name,
                passed = passed,
                executionSteps = executionSteps,
                error = null,
            )
        } catch (e: Exception) {
            logger.error("Error executing test '${test.name}': ${e.message}", e)

            executionSteps.add(
                ExecutionStep(
                    stepNumber = executionSteps.size + 1,
                    type = StepType.ERROR,
                    content = "Error: ${e.message ?: "Unknown error"}",
                ),
            )

            return TestResultDto(
                testId = test.id,
                testName = test.name,
                passed = false,
                executionSteps = executionSteps,
                error = e.message,
            )
        }
    }

    private fun executeSnippetCode(
        code: String,
        inputs: List<String>,
    ): List<String> {
        // SIMULACIÓN: Aquí deberías ejecutar el código real
        // Por ahora, simulamos algunos outputs basados en los inputs

        logger.debug("Executing code with ${inputs.size} inputs")

        // Ejemplo simple: si el código contiene "println", simulamos outputs
        val outputs = mutableListOf<String>()

        // Simulación básica
        inputs.forEach { input ->
            outputs.add("Processed: $input")
        }

        return outputs
    }

    private fun compareOutputs(
        actual: List<String>,
        expected: List<String>,
    ): Boolean {
        if (actual.size != expected.size) {
            logger.debug("Output size mismatch: expected ${expected.size}, got ${actual.size}")
            return false
        }

        val matches =
            actual.zip(expected).all { (actualOutput, expectedOutput) ->
                actualOutput.trim() == expectedOutput.trim()
            }

        if (!matches) {
            logger.debug("Output content mismatch")
        }

        return matches
    }
}
