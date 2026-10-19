package com.grupo14IngSis.snippetSearcherRunner.storage

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import org.springframework.stereotype.Component

/**
 * Único punto del Runner que sabe dónde vive el CÓDIGO de un snippet: el asset service
 * (bucket emulado con Azurite), container `snippets`, key = snippetId.
 *
 * Antes cada llamador elegía el container ("snippets" o "snippet") y el formateo guardaba en
 * los dos "por robustez"; ahora hay un solo container y los errores de escritura no se ocultan.
 */
@Component
class SnippetStorage(
    private val assets: AssetServiceClient,
) {
    companion object {
        const val CONTAINER = "snippets"

        /**
         * Copia del código tal como lo escribió el usuario (US #13: descargar la versión original).
         * El formateo (botón o cambio de reglas) reemplaza el contenido principal, pero no esta copia.
         */
        fun originalKey(snippetId: String) = "${snippetId}_original"
    }

    /** Contenido del snippet, o null si no existe. Errores de red/5xx se propagan (son transitorios). */
    fun get(snippetId: String): String? = assets.getAsset(CONTAINER, snippetId)

    /**
     * Código original (el último que escribió el usuario al crear o editar). Los snippets creados antes
     * de guardar esta copia no la tienen: para ellos el original es el contenido actual.
     */
    fun getOriginal(snippetId: String): String? = assets.getAsset(CONTAINER, originalKey(snippetId)) ?: get(snippetId)

    /**
     * Guarda código escrito por el usuario (crear / editar): es a la vez el contenido actual y el original.
     * Lanza [SnippetStorageException] si alguna de las dos escrituras falla.
     */
    fun saveUserCode(
        snippetId: String,
        content: String,
    ) {
        save(snippetId, content)
        val status = assets.postAsset(CONTAINER, originalKey(snippetId), content)
        if (status !in 200..299) {
            throw SnippetStorageException("Asset service answered $status while saving the original of snippet $snippetId")
        }
    }

    fun exists(snippetId: String): Boolean = get(snippetId) != null

    /** Crea o reemplaza el contenido. Lanza [SnippetStorageException] si el asset service no lo guardó. */
    fun save(
        snippetId: String,
        content: String,
    ) {
        val status = assets.postAsset(CONTAINER, snippetId, content)
        if (status !in 200..299) {
            throw SnippetStorageException("Asset service answered $status while saving snippet $snippetId")
        }
    }

    /** Devuelve false si el snippet no existía. También borra la copia del original, si la hay. */
    fun delete(snippetId: String): Boolean {
        // Puede no existir (snippets viejos): no importa el resultado
        runCatching { assets.deleteAsset(CONTAINER, originalKey(snippetId)) }
        val status = assets.deleteAsset(CONTAINER, snippetId)
        return when {
            status in 200..299 -> true
            status == 404 -> false
            else -> throw SnippetStorageException("Asset service answered $status while deleting snippet $snippetId")
        }
    }
}

class SnippetStorageException(
    message: String,
) : RuntimeException(message)
