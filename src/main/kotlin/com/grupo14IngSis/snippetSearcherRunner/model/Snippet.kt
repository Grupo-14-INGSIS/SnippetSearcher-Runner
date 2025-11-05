package com.grupo14IngSis.snippetSearcherRunner.model

data class Snippet(
    val id: Long? = null,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val code: String,
    val ownerId: String,
)
