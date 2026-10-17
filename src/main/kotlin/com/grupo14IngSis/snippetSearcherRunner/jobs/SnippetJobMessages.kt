package com.grupo14IngSis.snippetSearcherRunner.jobs

/*
 * Esquema de los mensajes que viajan por Redis Streams. Es el único acoplamiento entre
 * productor y consumidor, por eso la misma definición existe (idéntica) en el App.
 *
 *   snippet-jobs         App -> Runner   "procesá este snippet"
 *   snippet-job-results  Runner -> App   "este es el resultado"
 */

enum class JobTask(
    val wire: String,
) {
    LINT("lint"),
    FORMAT("format"),
    ;

    companion object {
        fun fromWire(value: String?): JobTask? = entries.firstOrNull { it.wire == value?.lowercase() }
    }
}

data class SnippetJob(
    val task: JobTask,
    val snippetId: String,
    // Dueño del snippet: se aplican sus reglas de linteo/formateo
    val userId: String,
    val language: String,
    val version: String,
) {
    companion object {
        fun fromFields(fields: Map<String, String>): SnippetJob? {
            val task = JobTask.fromWire(fields["task"]) ?: return null
            val snippetId = fields["snippetId"]?.takeIf { it.isNotBlank() } ?: return null
            val userId = fields["userId"]?.takeIf { it.isNotBlank() } ?: return null
            return SnippetJob(
                task,
                snippetId,
                userId,
                fields["language"]?.takeIf { it.isNotBlank() } ?: "printscript",
                fields["version"]?.takeIf { it.isNotBlank() } ?: "1.1",
            )
        }
    }
}

enum class JobOutcome(
    val wire: String,
) {
    COMPLIANT("compliant"),
    NOT_COMPLIANT("not-compliant"),
    FORMATTED("formatted"),
    FAILED("failed"),
}

data class SnippetJobResult(
    val task: JobTask,
    val snippetId: String,
    val outcome: JobOutcome,
    val detail: String = "",
) {
    fun toFields(): Map<String, String> =
        mapOf(
            "task" to task.wire,
            "snippetId" to snippetId,
            "outcome" to outcome.wire,
            "detail" to detail,
        )
}
