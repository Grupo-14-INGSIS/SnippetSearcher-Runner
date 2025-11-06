package com.grupo14IngSis.snippetSearcherRunner.dto

sealed class SnippetExecutionEvent {
    data class Output(val content: String) : SnippetExecutionEvent()
    data class InputRequest(val prompt: String) : SnippetExecutionEvent()
    data class Error(val message: String) : SnippetExecutionEvent()
    object Completed : SnippetExecutionEvent()
}