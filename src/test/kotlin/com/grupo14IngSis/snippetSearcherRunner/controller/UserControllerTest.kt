package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class UserControllerTest {
    private val userService: UserService = mockk(relaxed = true)
    private val userController = UserController(userService)

    @Test
    fun `addUser should return 200 when user is created`() {
        val userId = "test-user"
        every { userService.check(userId) } returns false

        val response = userController.addUser(userId)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify { userService.addUser(eq(userId), any(), any()) }
    }

    @Test
    fun `addUser should return 409 when user already exists`() {
        val userId = "existing-user"
        every { userService.check(userId) } returns true

        val response = userController.addUser(userId)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("User already exists", response.body)
    }

    @Test
    fun `addUser should return 400 on illegal state exception`() {
        val userId = "test-user"
        every { userService.check(userId) } returns false
        every { userService.addUser(eq(userId), any(), any()) } throws IllegalStateException("Test exception")

        val response = userController.addUser(userId)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Test exception", response.body)
    }

    @Test
    fun `deleteUser should return 204 when user is deleted`() {
        val userId = "test-user"

        val response = userController.deleteUser(userId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify { userService.deleteUser(userId) }
    }

    @Test
    fun `deleteUser should return 400 on illegal argument exception`() {
        val userId = "test-user"
        every { userService.deleteUser(userId) } throws IllegalArgumentException("Test exception")

        val response = userController.deleteUser(userId)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Test exception", response.body)
    }
}
