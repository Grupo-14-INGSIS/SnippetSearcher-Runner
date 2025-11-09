//package com.grupo14IngSis.snippetSearcherRunner.events
//
//import com.grupo14IngSis.snippetSearcherRunner.formatting.FormattingJobService
//import org.slf4j.LoggerFactory
//import org.springframework.context.event.EventListener
//import org.springframework.stereotype.Component
//
//@Component
//class RuleEnabledEventListener(
//    private val formattingJobService: FormattingJobService,
//) {
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    @EventListener
//    fun onRuleEnabled(event: RuleEnabledEvent) {
//        logger.info("Received RuleEnabledEvent: rule=${event.ruleId}, user=${event.userId}, ruleName=${event.ruleName}")
//
//        try {
//            val job =
//                formattingJobService.createFormattingJob(
//                    ruleId = event.ruleId,
//                    userId = event.userId,
//                )
//
//            logger.info("Created formatting job ${job.id} with ${job.totalSnippets} snippets to process for user ${event.userId}")
//        } catch (e: Exception) {
//            logger.error("Error creating formatting job for rule ${event.ruleId} and user ${event.userId}", e)
//        }
//    }
//}
//
///**
// * Evento que se dispara cuando un administrador habilita una regla de formateo
// */
//data class RuleEnabledEvent(
//    val ruleId: String,
//    val userId: Int,
//    val ruleName: String,
//)
