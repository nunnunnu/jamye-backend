package org.jy.jamye.ui

import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.ui.post.UserPostDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/join")
    fun createUser(@RequestBody data: UserPostDto) : ResponseDto<Long> {
        val sequence = userService.createUser(
            UserDto(
                id = data.id,
                email = data.email,
                password = data.password,
            )
        )
        return ResponseDto(data = sequence, status = HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody data: UserPostDto.Login) : ResponseDto<UserDto.UserLoginDto> {
        val user = userService.login(data.id, data.password)
        return ResponseDto(data = user, status = HttpStatus.OK)
    }
}