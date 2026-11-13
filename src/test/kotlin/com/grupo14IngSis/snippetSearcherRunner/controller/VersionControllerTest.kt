package com.grupo14IngSis.snippetSearcherRunner.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class VersionControllerTest {
    @Test
    fun `reports the PrintScript version the Runner was built with`() {
        val response = VersionController("2.0.12").version()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mapOf("printscript" to "2.0.12"), response.body)
    }
}
