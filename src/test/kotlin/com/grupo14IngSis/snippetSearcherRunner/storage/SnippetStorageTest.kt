package com.grupo14IngSis.snippetSearcherRunner.storage

import com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SnippetStorageTest {
    private val assets: AssetServiceClient = mockk()
    private val storage = SnippetStorage(assets)

    @Test
    fun `everything goes to the single snippets container`() {
        every { assets.getAsset("snippets", "s1") } returns "code"
        every { assets.postAsset("snippets", "s1", "new") } returns 201

        assertEquals("code", storage.get("s1"))
        storage.save("s1", "new")

        verify { assets.postAsset("snippets", "s1", "new") }
    }

    @Test
    fun `save fails loudly when the asset service does not store the content`() {
        every { assets.postAsset("snippets", "s1", "new") } returns 500

        assertThrows<SnippetStorageException> { storage.save("s1", "new") }
    }

    @Test
    fun `delete distinguishes deleted, missing and error`() {
        every { assets.deleteAsset("snippets", "ok") } returns 204
        every { assets.deleteAsset("snippets", "missing") } returns 404
        every { assets.deleteAsset("snippets", "error") } returns 500

        assertTrue(storage.delete("ok"))
        assertFalse(storage.delete("missing"))
        assertThrows<SnippetStorageException> { storage.delete("error") }
    }

    @Test
    fun `exists is based on the content`() {
        every { assets.getAsset("snippets", "s1") } returns null

        assertFalse(storage.exists("s1"))
    }
}
