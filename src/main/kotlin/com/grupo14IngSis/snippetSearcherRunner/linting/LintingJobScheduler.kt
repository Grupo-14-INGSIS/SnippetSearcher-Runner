//package com.grupo14IngSis.snippetSearcherRunner.linting
//
//import kotlinx.coroutines.runBlocking
//import org.slf4j.LoggerFactory
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//
//@Component
//class LintingJobScheduler(
//    private val lintingJobService: LintingJobService,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    @Scheduled(fixedDelay = 5000) // Cada 5 segundos
//    fun processJobs() {
//        runBlocking {
//            try {
//                lintingJobService.processNextJob()
//            } catch (e: Exception) {
//                logger.error("Error processing linting job", e)
//            }
//        }
//    }
//}
