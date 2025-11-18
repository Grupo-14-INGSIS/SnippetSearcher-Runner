//package com.grupo14IngSis.snippetSearcherRunner.linting
//
//import com.grupo14IngSis.snippetSearcherRunner.client.SnippetServiceClient
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJob
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJobStatus
//import org.springframework.stereotype.Service
//import java.time.LocalDateTime
//import java.util.UUID
//
//@Service
//class LintingJobService(
//    private val lintingJobRepository: LintingJobRepository,
//    private val lintingJobProcessor: LintingJobProcessor,
//    private val snippetServiceClient: SnippetServiceClient,
//) {
//    fun createLintingJob(
//        ruleId: String,
//        userId: String,
//    ): LintingJob {
//        val totalSnippets = snippetServiceClient.countSnippetsByUserId(userId)
//
//        val job =
//            LintingJob(
//                id = UUID.randomUUID().toString(),
//                ruleId = ruleId,
//                userId = userId,
//                totalSnippets = totalSnippets,
//                status = LintingJobStatus.PENDING,
//                createdAt = LocalDateTime.now(),
//            )
//
//        return lintingJobRepository.save(job)
//    }
//
//    suspend fun processNextJob() {
//        val job = lintingJobRepository.getNextPendingJob() ?: return
//        lintingJobProcessor.processJob(job)
//    }
//
//    fun getJobStatus(jobId: String): LintingJob? {
//        return lintingJobRepository.findById(jobId)
//    }
//}
