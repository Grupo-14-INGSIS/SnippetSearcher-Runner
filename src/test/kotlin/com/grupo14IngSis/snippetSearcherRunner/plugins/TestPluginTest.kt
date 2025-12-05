package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestPluginTest {
    @Test
    fun `run with snippet should return formatted string`() {
        val testPlugin = TestPlugin()
        val snippet = "this is a test snippet"
        val expected = "received: \"$snippet\""
        val result = testPlugin.run(snippet, emptyMap())
        assertEquals(expected, result)
    }

    @Test
    fun `run with null snippet should return formatted string for null`() {
        val testPlugin = TestPlugin()
        val expected = "received: \"null\""
        val result = testPlugin.run(null, emptyMap())
        assertEquals(expected, result)
    }
}
