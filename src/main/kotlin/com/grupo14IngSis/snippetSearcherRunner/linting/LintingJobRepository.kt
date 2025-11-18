//package com.grupo14IngSis.snippetSearcherRunner.linting
//
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJob
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingJobStatus
//import com.grupo14IngSis.snippetSearcherRunner.linting.dto.LintingResult
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.stereotype.Repository
//import java.time.Duration
//
//@Repository
//class LintingJobRepository(
//    private val redisTemplate: RedisTemplate<String, Any>,
//) {
//    companion object {
//        private const val JOB_KEY_PREFIX = "linting:job:"
//        private const val PENDING_JOBS_KEY = "linting:jobs:pending"
//        private const val JOB_TTL_DAYS = 7L
//    }
//
//    fun save(job: LintingJob): LintingJob {
//        val key = "$JOB_KEY_PREFIX${job.id}"
//        redisTemplate.opsForValue().set(key, job, Duration.ofDays(JOB_TTL_DAYS))
//
//        if (job.status == LintingJobStatus.PENDING) {
//            redisTemplate.opsForList().rightPush(PENDING_JOBS_KEY, job.id)
//        }
//
//        return job
//    }
//
//    fun findById(jobId: String): LintingJob? {
//        val key = "$JOB_KEY_PREFIX$jobId"
//        return redisTemplate.opsForValue().get(key) as? LintingJob
//    }
//
//    fun getNextPendingJob(): LintingJob? {
//        val jobId = redisTemplate.opsForList().leftPop(PENDING_JOBS_KEY) as? String
//        return jobId?.let { findById(it) }
//    }
//
//    fun updateProgress(
//        jobId: String,
//        processedSnippets: Int,
//        passedSnippets: Int,
//        lastSnippetId: String,
//        status: LintingJobStatus,
//    ) {
//        findById(jobId)?.let { job ->
//            save(
//                job.copy(
//                    processedSnippets = processedSnippets,
//                    passedSnippets = passedSnippets,
//                    lastProcessedSnippetId = lastSnippetId,
//                    status = status,
//                ),
//            )
//        }
//    }
//
//    fun addLintingResult(
//        jobId: String,
//        snippetId: String,
//        result: LintingResult,
//    ) {
//        findById(jobId)?.let { job ->
//            val updatedResults = job.lintingResults + (snippetId to result)
//            save(
//                job.copy(
//                    lintingResults = updatedResults,
//                    processedSnippets = job.processedSnippets + 1,
//                    passedSnippets = if (result.passed) job.passedSnippets + 1 else job.passedSnippets,
//                    lastProcessedSnippetId = snippetId,
//                ),
//            )
//        }
//    }
//
//    fun markAsFailed(
//        jobId: String,
//        failedSnippetId: String,
//        error: String,
//    ) {
//        findById(jobId)?.let { job ->
//            save(
//                job.copy(
//                    failedSnippets = job.failedSnippets + failedSnippetId,
//                    errorMessage = error,
//                    status = LintingJobStatus.FAILED,
//                ),
//            )
//        }
//    }
//}
