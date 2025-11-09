//package com.grupo14IngSis.snippetSearcherRunner.formatting
//
//import kotlinx.coroutines.runBlocking
//import org.slf4j.LoggerFactory
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//
//@Component
//class FormattingJobScheduler(
//    private val formattingJobService: FormattingJobService,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    @Scheduled(fixedDelay = 5000)
//    fun processJobs() {
//        runBlocking {
//            try {
//                formattingJobService.processNextJob()
//            } catch (e: Exception) {
//                logger.error("Error processing formatting job", e)
//            }
//        }
//    }
//}
