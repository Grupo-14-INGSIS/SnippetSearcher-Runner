//package com.grupo14IngSis.snippetSearcherRunner.linting
//
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetDto
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetServiceClient
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintViolation
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJob
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJobStatus
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingResult
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.SnippetLintResult
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import org.springframework.web.reactive.function.client.WebClient
//import reactor.util.retry.Retry
//import java.time.Duration
//
//@Service
//class LintingJobProcessor(
//    private val webClient: WebClient,
//    private val lintingJobRepository: LintingJobRepository,
//    private val snippetServiceClient: SnippetServiceClient,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//    private val linterUrl = System.getenv("LINTER_URL") ?: "http://linter:8080"
//
//    companion object {
//        private const val BATCH_SIZE = 10
//        private const val MAX_RETRIES = 3L
//    }
//
//    suspend fun processJob(job: LintingJob) {
//        try {
//            logger.info("Starting linting job ${job.id} for rule ${job.ruleId}")
//
//            lintingJobRepository.save(
//                job.copy(status = LintingJobStatus.PROCESSING),
//            )
//
//            val snippets = getSnippetsToLint(job)
//
//            if (snippets.isEmpty()) {
//                logger.warn("No snippets found for job ${job.id}")
//                lintingJobRepository.save(
//                    job.copy(
//                        status = LintingJobStatus.COMPLETED,
//                        processedSnippets = 0,
//                    ),
//                )
//                return
//            }
//
//            val totalBatches = (snippets.size + BATCH_SIZE - 1) / BATCH_SIZE
//            logger.info("Linting ${snippets.size} snippets in $totalBatches batches for job ${job.id}")
//
//            snippets.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
//                logger.info("Processing batch ${batchIndex + 1}/$totalBatches for job ${job.id}")
//                processBatch(job, batch)
//            }
//
//            val updatedJob = lintingJobRepository.findById(job.id)
//            lintingJobRepository.save(
//                updatedJob!!.copy(
//                    status = LintingJobStatus.COMPLETED,
//                    processedSnippets = snippets.size,
//                ),
//            )
//
//            logger.info("Completed linting job ${job.id}. Processed ${snippets.size} snippets, ${updatedJob.passedSnippets} passed")
//        } catch (e: Exception) {
//            logger.error("Error processing linting job ${job.id}", e)
//            handleJobFailure(job, e)
//        }
//    }
//
//    private suspend fun processBatch(
//        job: LintingJob,
//        snippets: List<SnippetDto>,
//    ) {
//        snippets.forEach { snippet ->
//            try {
//                logger.debug("Linting snippet ${snippet.id}")
//                val result = lintSnippet(snippet, job.ruleId)
//
//                if (result.success) {
//                    // Guardar resultado del linting
//                    val lintingResult =
//                        LintingResult(
//                            snippetId = snippet.id,
//                            passed = result.passed,
//                            violations = result.violations,
//                        )
//
//                    lintingJobRepository.addLintingResult(job.id, snippet.id, lintingResult)
//
//                    logger.debug(
//                        "Snippet ${snippet.id} linting result: ${if (result.passed) "PASSED" else "FAILED"} " +
//                            "with ${result.violations.size} violations",
//                    )
//                } else {
//                    logger.warn("Failed to lint snippet ${snippet.id}: ${result.error}")
//                    lintingJobRepository.markAsFailed(
//                        jobId = job.id,
//                        failedSnippetId = snippet.id,
//                        error = result.error ?: "Unknown linting error",
//                    )
//                }
//            } catch (e: Exception) {
//                logger.error("Error processing snippet ${snippet.id}", e)
//                lintingJobRepository.markAsFailed(
//                    jobId = job.id,
//                    failedSnippetId = snippet.id,
//                    error = e.message ?: "Unknown error",
//                )
//            }
//        }
//    }
//
//    private suspend fun lintSnippet(
//        snippet: SnippetDto,
//        ruleId: String,
//    ): SnippetLintResult {
//        return try {
//            logger.debug("Calling linter service for snippet ${snippet.id}")
//
//            // El linter devuelve una lista de violaciones
//            val response =
//                webClient.post()
//                    .uri("$linterUrl/lint")
//                    .bodyValue(
//                        mapOf(
//                            "content" to snippet.content,
//                            "ruleId" to ruleId,
//                            "language" to snippet.language,
//                        ),
//                    )
//                    .retrieve()
//                    .bodyToMono(LinterResponse::class.java)
//                    .retryWhen(
//                        Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
//                            .maxBackoff(Duration.ofSeconds(10))
//                            .doBeforeRetry { signal ->
//                                logger.warn("Retrying lint request for snippet ${snippet.id}, attempt ${signal.totalRetries() + 1}")
//                            },
//                    )
//                    .block()
//
//            SnippetLintResult(
//                snippetId = snippet.id,
//                success = true,
//                passed = response?.violations?.isEmpty() ?: true,
//                violations = response?.violations ?: emptyList(),
//            )
//        } catch (e: Exception) {
//            logger.error("Error calling linter service for snippet ${snippet.id}", e)
//            SnippetLintResult(
//                snippetId = snippet.id,
//                success = false,
//                error = e.message ?: "Linter service error",
//            )
//        }
//    }
//
//    private fun getSnippetsToLint(job: LintingJob): List<SnippetDto> {
//        return try {
//            if (job.lastProcessedSnippetId != null) {
//                logger.info("Resuming job ${job.id} from snippet ${job.lastProcessedSnippetId}")
//                snippetServiceClient.getSnippetsAfter(job.userId, job.lastProcessedSnippetId)
//            } else {
//                logger.info("Starting new linting job ${job.id} for user ${job.userId}")
//                snippetServiceClient.getSnippetsByUserId(job.userId)
//            }
//        } catch (e: Exception) {
//            logger.error("Error fetching snippets for job ${job.id}", e)
//            emptyList()
//        }
//    }
//
//    private fun handleJobFailure(
//        job: LintingJob,
//        error: Exception,
//    ) {
//        if (job.retryCount < MAX_RETRIES) {
//            logger.warn("Job ${job.id} failed, scheduling retry ${job.retryCount + 1}/$MAX_RETRIES")
//            lintingJobRepository.save(
//                job.copy(
//                    status = LintingJobStatus.PENDING,
//                    retryCount = job.retryCount + 1,
//                    errorMessage = "Retry ${job.retryCount + 1}: ${error.message}",
//                ),
//            )
//        } else {
//            logger.error("Job ${job.id} failed after $MAX_RETRIES retries")
//            lintingJobRepository.save(
//                job.copy(
//                    status = LintingJobStatus.FAILED,
//                    errorMessage = "Max retries exceeded: ${error.message}",
//                ),
//            )
//        }
//    }
//}
//
//// Response del servicio de linting
//data class LinterResponse(
//    val violations: List<LintViolation>,
//)
