//package com.grupo14IngSis.snippetSearcherRunner.formatting
//
//import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.FormattingJob
//import com.grupo14IngSis.snippetSearcherRunner.formatting.dto.FormattingJobStatus
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.stereotype.Repository
//import java.time.Duration
//
//@Repository
//class FormattingJobRepository(
//    private val redisTemplate: RedisTemplate<String, Any>,
//) {
//    companion object {
//        private const val JOB_KEY_PREFIX = "formatting:job:"
//        private const val PENDING_JOBS_KEY = "formatting:jobs:pending"
//        private const val JOB_TTL_DAYS = 7L
//    }
//
//    fun save(job: FormattingJob): FormattingJob {
//        val key = "$JOB_KEY_PREFIX${job.id}"
//        redisTemplate.opsForValue().set(key, job, Duration.ofDays(JOB_TTL_DAYS))
//
//        if (job.status == FormattingJobStatus.PENDING) {
//            redisTemplate.opsForList().rightPush(PENDING_JOBS_KEY, job.id)
//        }
//
//        return job
//    }
//
//    fun findById(jobId: String): FormattingJob? {
//        val key = "$JOB_KEY_PREFIX$jobId"
//        return redisTemplate.opsForValue().get(key) as? FormattingJob
//    }
//
//    fun getNextPendingJob(): FormattingJob? {
//        val jobId = redisTemplate.opsForList().leftPop(PENDING_JOBS_KEY) as? String
//        return jobId?.let { findById(it) }
//    }
//
//    fun updateProgress(
//        jobId: String,
//        processedSnippets: Int,
//        lastSnippetId: String,
//        status: FormattingJobStatus,
//    ) {
//        findById(jobId)?.let { job ->
//            save(
//                job.copy(
//                    processedSnippets = processedSnippets,
//                    status = status,
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
//                    status = FormattingJobStatus.FAILED,
//                ),
//            )
//        }
//    }
//}
