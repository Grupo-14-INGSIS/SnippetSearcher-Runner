//package com.grupo14IngSis.snippetSearcherRunner.service
//
//import com.grupo14IngSis.snippetSearcherRunner.dto.*
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetServiceClient
//import org.springframework.stereotype.Service
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.SharedFlow
//import java.util.concurrent.ConcurrentHashMap
//import java.io.*
//
//@Service
//class SnippetExecutionService(
//    private val snippetServiceClient: SnippetServiceClient
//) {
//    private val executions = ConcurrentHashMap<String, ExecutionContext>()
//
//    suspend fun startExecution(request: SnippetExecutionRequest): String {
//        val executionId = generateExecutionId()
//        val snippet = snippetServiceClient.getSnippet(request.snippetId)
//
//        val eventFlow = MutableSharedFlow<SnippetExecutionEvent>()
//        val inputStream = PipedInputStream()
//        val outputStream = PipedOutputStream(inputStream)
//
//        val context = ExecutionContext(
//            id = executionId,
//            snippetId = request.snippetId,
//            eventFlow = eventFlow,
//            inputStream = inputStream,
//            outputStream = outputStream,
//            isRunning = true
//        )
//
//        executions[executionId] = context
//
//        // Ejecutar en un hilo separado
//        executeSnippetAsync(context, snippet.content, request.language)
//
//        return executionId
//    }
//
//    fun getEventFlow(executionId: String): SharedFlow<SnippetExecutionEvent>? {
//        return executions[executionId]?.eventFlow
//    }
//
//    suspend fun provideInput(executionId: String, input: String) {
//        val context = executions[executionId] ?: throw IllegalArgumentException("Execution not found")
//
//        if (!context.isRunning) {
//            throw IllegalStateException("Execution is not running")
//        }
//
//        // Enviar input al proceso
//        context.outputStream.write("$input\n".toByteArray())
//        context.outputStream.flush()
//    }
//
//    suspend fun cancelExecution(executionId: String) {
//        val context = executions[executionId] ?: return
//        context.isRunning = false
//        context.outputStream.close()
//        context.inputStream.close()
//        executions.remove(executionId)
//        context.eventFlow.emit(SnippetExecutionEvent.Completed)
//    }
//
//    private fun executeSnippetAsync(
//        context: ExecutionContext,
//        code: String,
//        language: String
//    ) {
//        Thread {
//            try {
//                val customInput = BufferedReader(InputStreamReader(context.inputStream))
//                val customOutput = PrintWriter(OutputStreamWriter(object : OutputStream() {
//                    override fun write(b: Int) {
//                        // Emitir cada carácter como output
//                        context.eventFlow.tryEmit(
//                            SnippetExecutionEvent.Output(b.toChar().toString())
//                        )
//                    }
//                }), true)
//
//                // Aquí integrarías con PrintScript o el intérprete correspondiente
//                executeWithInterpreter(
//                    code = code,
//                    language = language,
//                    input = customInput,
//                    output = customOutput,
//                    onInputRequest = { prompt ->
//                        context.eventFlow.tryEmit(SnippetExecutionEvent.InputRequest(prompt))
//                    }
//                )
//
//                context.eventFlow.tryEmit(SnippetExecutionEvent.Completed)
//            } catch (e: Exception) {
//                context.eventFlow.tryEmit(SnippetExecutionEvent.Error(e.message ?: "Unknown error"))
//            } finally {
//                context.isRunning = false
//                executions.remove(context.id)
//            }
//        }.start()
//    }
//
//    private fun executeWithInterpreter(
//        code: String,
//        language: String,
//        input: BufferedReader,
//        output: PrintWriter,
//        onInputRequest: (String) -> Unit
//    ) {
//        // Implementación específica para ejecutar PrintScript
//        // Deberás integrar con el intérprete de PrintScript aquí
//
//        // Ejemplo simplificado:
//        when (language.lowercase()) {
//            "printscript" -> {
//                // Integración con PrintScript interpreter
//                // interpreter.execute(code, input, output, onInputRequest)
//
//                // Placeholder - reemplazar con la integración real
//                output.println("Executing snippet...")
//                output.println("Output from snippet")
//            }
//            else -> throw IllegalArgumentException("Unsupported language: $language")
//        }
//    }
//
//    private fun generateExecutionId(): String =
//        "${System.currentTimeMillis()}-${(1000..9999).random()}"
//
//    data class ExecutionContext(
//        val id: String,
//        val snippetId: String,
//        val eventFlow: MutableSharedFlow<SnippetExecutionEvent>,
//        val inputStream: PipedInputStream,
//        val outputStream: PipedOutputStream,
//        var isRunning: Boolean
//    )
//}