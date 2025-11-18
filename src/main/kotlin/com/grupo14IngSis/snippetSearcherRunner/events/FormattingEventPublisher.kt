//package com.grupo14IngSis.snippetSearcherRunner.events
//
//import org.slf4j.LoggerFactory
//import org.springframework.context.ApplicationEventPublisher
//import org.springframework.stereotype.Component
//
//@Component
//class FormattingEventPublisher(
//    private val applicationEventPublisher: ApplicationEventPublisher,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    fun publishRuleEnabledEvent(
//        ruleId: String,
//        userId: Int,
//        ruleName: String,
//    ) {
//        logger.info("Publishing RuleEnabledEvent for rule=$ruleId, user=$userId")
//
//        val event =
//            RuleEnabledEvent(
//                ruleId = ruleId,
//                userId = userId,
//                ruleName = ruleName,
//            )
//
//        applicationEventPublisher.publishEvent(event)
//
//        logger.debug("RuleEnabledEvent published successfully")
//    }
//
//    fun publishFormattingJobCompletedEvent(
//        jobId: String,
//        userId: String,
//        totalProcessed: Int,
//        totalFailed: Int,
//    ) {
//        logger.info("Publishing FormattingJobCompletedEvent for job=$jobId")
//
//        val event =
//            FormattingJobCompletedEvent(
//                jobId = jobId,
//                userId = userId,
//                totalProcessed = totalProcessed,
//                totalFailed = totalFailed,
//            )
//
//        applicationEventPublisher.publishEvent(event)
//    }
//
//    fun publishFormattingJobFailedEvent(
//        jobId: String,
//        userId: String,
//        errorMessage: String,
//    ) {
//        logger.warn("Publishing FormattingJobFailedEvent for job=$jobId")
//
//        val event =
//            FormattingJobFailedEvent(
//                jobId = jobId,
//                userId = userId,
//                errorMessage = errorMessage,
//            )
//
//        applicationEventPublisher.publishEvent(event)
//    }
//
//    fun publishRuleModifiedEvent(
//        ruleId: String,
//        userId: String,
//        ruleName: String,
//    ) {
//        logger.info("Publishing RuleModifiedEvent for rule=$ruleId, user=$userId")
//
//        val event =
//            RuleModifiedEvent(
//                ruleId = ruleId,
//                userId = userId,
//                ruleName = ruleName,
//            )
//
//        applicationEventPublisher.publishEvent(event)
//
//        logger.debug("RuleModifiedEvent published successfully")
//    }
//}
//
//data class FormattingJobCompletedEvent(
//    val jobId: String,
//    val userId: String,
//    val totalProcessed: Int,
//    val totalFailed: Int,
//)
//
//data class FormattingJobFailedEvent(
//    val jobId: String,
//    val userId: String,
//    val errorMessage: String,
//)
//
//data class RuleModifiedEvent(
//    val ruleId: String,
//    val userId: String,
//    val ruleName: String,
//)
