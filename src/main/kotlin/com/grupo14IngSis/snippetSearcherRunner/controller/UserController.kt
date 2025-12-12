package com.grupo14IngSis.snippetSearcherRunner.controller

import com.grupo14IngSis.snippetSearcherRunner.dto.UserCreationRequest
import com.grupo14IngSis.snippetSearcherRunner.service.PrintScriptRulesProvider
import com.grupo14IngSis.snippetSearcherRunner.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/")
class UserController(
    private val userService: UserService,
) {
    /**
     * PUT    /api/v1/users/{userId}
     *
     * Create an entry for the user. This adds all rules with default values
     */
    @PutMapping("/{userId}")
    fun addUser(
        @PathVariable userId: String,
    ): ResponseEntity<*> {
        try {
            val rulesProvider = PrintScriptRulesProvider()
            val formattingRules = UserCreationRequest(listOf(rulesProvider.getFormattingRules(null)))
            val lintingRules = UserCreationRequest(listOf(rulesProvider.getLintingRules(null)))
            val userExists = userService.check(userId)
            if (userExists) {
                return ResponseEntity.status(409).body("User already exists")
            }
            userService.addUser(userId, formattingRules, lintingRules)
            return ResponseEntity.ok().build<Unit>()
        } catch (e: IllegalStateException) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }

    /**
     * DELETE /api/v1/users/{userId}
     *
     * Delete all rules linked to the user
     *
     * Meant for db cleaning when deleting a user
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: String,
    ): ResponseEntity<Any> {
        try {
            userService.deleteUser(userId)
            return ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }
}
