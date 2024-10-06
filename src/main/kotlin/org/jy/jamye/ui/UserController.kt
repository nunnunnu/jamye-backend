package org.jy.jamye.ui

import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.ui.post.UserPostDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody data: UserPostDto) {
        userService.createUser(UserDto(id= data.id, email = data.email, password = data.password, nickname = data.nickname))
    }
}