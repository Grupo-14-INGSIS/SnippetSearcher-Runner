//package com.grupo14IngSis.snippetSearcherRunner.events
//
//import com.grupo14IngSis.snippetSearcherRunner.linting.LintingJobService
//import org.slf4j.LoggerFactory
//import org.springframework.context.event.EventListener
//import org.springframework.stereotype.Component
//
//@Component
//class RuleModifiedEventListener(
//    private val lintingJobService: LintingJobService,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    @EventListener
//    fun onRuleModified(event: RuleModifiedEvent) {
//        logger.info("Received RuleModifiedEvent: rule=${event.ruleId}, user=${event.userId}, ruleName=${event.ruleName}")
//
//        try {
//            val job =
//                lintingJobService.createLintingJob(
//                    ruleId = event.ruleId,
//                    userId = event.userId,
//                )
//
//            logger.info("Created linting job ${job.id} with ${job.totalSnippets} snippets to lint for user ${event.userId}")
//        } catch (e: Exception) {
//            logger.error("Error creating linting job for rule ${event.ruleId} and user ${event.userId}", e)
//        }
//    }
//}
//
///**
// * Evento que se dispara cuando un administrador modifica una regla de linting
// */
//data class RuleModifiedEvent(
//    val ruleId: String,
//    val userId: String,
//    val ruleName: String,
//)
