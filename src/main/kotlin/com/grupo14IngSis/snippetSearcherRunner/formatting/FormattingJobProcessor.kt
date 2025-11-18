//package com.grupo14IngSis.snippetSearcherRunner.formatting
//
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetDto
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetServiceClient
//import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.FormattingJob
//import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.FormattingJobStatus
//import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.SnippetFormatResult
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import org.springframework.web.reactive.function.client.WebClient
//import reactor.util.retry.Retry
//import java.time.Duration
//
//@Service
//class FormattingJobProcessor(
//    private val webClient: WebClient,
//    private val formattingJobRepository: FormattingJobRepository,
//    private val snippetServiceClient: SnippetServiceClient,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//    private val formatterUrl = System.getenv("FORMATTER_URL") ?: "http://formatter:8080"
//
//    companion object {
//        private const val BATCH_SIZE = 10
//        private const val MAX_RETRIES = 3L
//    }
//
//    suspend fun processJob(job: FormattingJob) {
//        try {
//            logger.info("Starting formatting job ${job.id} for rule ${job.ruleId}")
//
//            // Actualizar estado a PROCESSING
//            formattingJobRepository.save(
//                job.copy(status = FormattingJobStatus.PROCESSING),
//            )
//
//            // Obtener snippets a formatear
//            val snippets = getSnippetsToFormat(job)
//
//            if (snippets.isEmpty()) {
//                logger.warn("No snippets found for job ${job.id}")
//                formattingJobRepository.save(
//                    job.copy(
//                        status = FormattingJobStatus.COMPLETED,
//                        processedSnippets = 0,
//                    ),
//                )
//                return
//            }
//
//            val totalBatches = (snippets.size + BATCH_SIZE - 1) / BATCH_SIZE
//            logger.info("Processing ${snippets.size} snippets in $totalBatches batches for job ${job.id}")
//
//            // Procesar snippets en lotes
//            snippets.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
//                logger.info("Processing batch ${batchIndex + 1}/$totalBatches for job ${job.id}")
//                processBatch(job, batch)
//            }
//
//            // Marcar como completado
//            formattingJobRepository.save(
//                job.copy(
//                    status = FormattingJobStatus.COMPLETED,
//                    processedSnippets = snippets.size,
//                ),
//            )
//
//            logger.info("Completed formatting job ${job.id}. Processed ${snippets.size} snippets, ${job.failedSnippets.size} failed")
//        } catch (e: Exception) {
//            logger.error("Error processing job ${job.id}", e)
//            handleJobFailure(job, e)
//        }
//    }
//
//    private suspend fun processBatch(
//        job: FormattingJob,
//        snippets: List<SnippetDto>,
//    ) {
//        snippets.forEach { snippet ->
//            try {
//                logger.debug("Formatting snippet ${snippet.snippetId}")
//                val result = formatSnippet(snippet, job.ruleId)
//
//                if (result.success && result.formattedContent != null) {
//                    // Actualizar snippet en el servicio
//                    val updated =
//                        snippetServiceClient.updateSnippetContent(
//                            snippet.snippetId,
//                            result.formattedContent,
//                        )
//
//                    if (updated) {
//                        // Actualizar progreso
//                        formattingJobRepository.updateProgress(
//                            jobId = job.id,
//                            processedSnippets = job.processedSnippets + 1,
//                            lastSnippetId = snippet.id,
//                            status = FormattingJobStatus.PROCESSING,
//                        )
//                        logger.debug("Successfully formatted and updated snippet ${snippet.id}")
//                    } else {
//                        logger.warn("Failed to update snippet ${snippet.id} after formatting")
//                        formattingJobRepository.markAsFailed(
//                            jobId = job.id,
//                            failedSnippetId = snippet.id,
//                            error = "Failed to update snippet content in snippet service",
//                        )
//                    }
//                } else {
//                    logger.warn("Failed to format snippet ${snippet.id}: ${result.error}")
//                    formattingJobRepository.markAsFailed(
//                        jobId = job.id,
//                        failedSnippetId = snippet.id,
//                        error = result.error ?: "Unknown formatting error",
//                    )
//                }
//            } catch (e: Exception) {
//                logger.error("Error processing snippet ${snippet.id}", e)
//                formattingJobRepository.markAsFailed(
//                    jobId = job.id,
//                    failedSnippetId = snippet.id,
//                    error = e.message ?: "Unknown error",
//                )
//            }
//        }
//    }
//
//    private suspend fun formatSnippet(
//        snippet: SnippetDto,
//        ruleId: String,
//    ): SnippetFormatResult {
//        return try {
//            logger.debug("Calling formatter service for snippet ${snippet.id}")
//
//            val response =
//                webClient.post()
//                    .uri("$formatterUrl/format")
//                    .bodyValue(
//                        mapOf(
//                            "content" to snippet.content,
//                            "ruleId" to ruleId,
//                            "language" to snippet.language,
//                        ),
//                    )
//                    .retrieve()
//                    .bodyToMono(String::class.java)
//                    .retryWhen(
//                        Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
//                            .maxBackoff(Duration.ofSeconds(10))
//                            .doBeforeRetry { signal ->
//                                logger.warn("Retrying format request for snippet ${snippet.id}, attempt ${signal.totalRetries() + 1}")
//                            },
//                    )
//                    .block()
//
//            SnippetFormatResult(
//                snippetId = snippet.id,
//                success = true,
//                formattedContent = response,
//            )
//        } catch (e: Exception) {
//            logger.error("Error calling formatter service for snippet ${snippet.id}", e)
//            SnippetFormatResult(
//                snippetId = snippet.id,
//                success = false,
//                error = e.message ?: "Formatter service error",
//            )
//        }
//    }
//
//    private fun getSnippetsToFormat(job: FormattingJob): List<SnippetDto> {
//        return try {
//            // Si es un reinicio, continúa desde el último snippet procesado
//            if (job.lastProcessedSnippetId != null) {
//                logger.info("Resuming job ${job.id} from snippet ${job.lastProcessedSnippetId}")
//                snippetServiceClient.getSnippetsAfter(job.userId, job.lastProcessedSnippetId)
//            } else {
//                logger.info("Starting new job ${job.id} for user ${job.userId}")
//                snippetServiceClient.getSnippetsByUserId(job.userId)
//            }
//        } catch (e: Exception) {
//            logger.error("Error fetching snippets for job ${job.id}", e)
//            emptyList()
//        }
//    }
//
//    private fun handleJobFailure(
//        job: FormattingJob,
//        error: Exception,
//    ) {
//        if (job.retryCount < MAX_RETRIES) {
//            logger.warn("Job ${job.id} failed, scheduling retry ${job.retryCount + 1}/$MAX_RETRIES")
//            formattingJobRepository.save(
//                job.copy(
//                    status = FormattingJobStatus.RETRYING,
//                    retryCount = job.retryCount + 1,
//                    errorMessage = error.message,
//                ),
//            )
//            // El job volverá a la cola PENDING para ser reprocesado
//            formattingJobRepository.save(
//                job.copy(
//                    status = FormattingJobStatus.PENDING,
//                    retryCount = job.retryCount + 1,
//                    errorMessage = "Retry ${job.retryCount + 1}: ${error.message}",
//                ),
//            )
//        } else {
//            logger.error("Job ${job.id} failed after $MAX_RETRIES retries")
//            formattingJobRepository.save(
//                job.copy(
//                    status = FormattingJobStatus.FAILED,
//                    errorMessage = "Max retries exceeded: ${error.message}",
//                ),
//            )
//        }
//    }
//}
