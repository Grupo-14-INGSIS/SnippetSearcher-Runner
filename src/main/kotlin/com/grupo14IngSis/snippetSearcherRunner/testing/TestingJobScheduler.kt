package com.grupo14lngSis.snippetSearcherRunner.testing

import com.grupo14lngSis.snippetSearcherRunner.events.SnippetUpdatedEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TestingJobScheduler(
    private val testingJobService: TestingJobService,
    private val testingJobRepository: TestingJobRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    @KafkaListener(topics = ["snippet.updated"], groupId = "runner-testing-group")
    fun handleSnippetUpdated(event: SnippetUpdatedEvent) {
        // Obtener los test cases del snippet (deberías tenerlos guardados)
        val testingRequest = testingJobRepository.getRequest(event.snippetId)

        if (testingRequest != null) {
            // Actualizar el contenido del snippet
            val updatedRequest = testingRequest.copy(content = event.content)

            // Ejecutar los tests automáticamente
            val result = testingJobService.executeTestingJob(updatedRequest)

            // Publicar el resultado en Kafka
            kafkaTemplate.send("testing.completed", result)
        }
    }
}
