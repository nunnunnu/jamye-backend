package org.jy.jamye.ui

import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.ui.post.LoginPostDto
import org.jy.jamye.ui.post.UserPasswordDto
import org.jy.jamye.ui.post.UserPostDto
import org.jy.jamye.ui.post.UserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/join")
    fun createUser(@RequestBody data: UserPostDto) : ResponseDto<Long> {
        val sequence = userService.createUser(
            UserDto(id = data.id, email = data.email, password = data.password,)
        )
        return ResponseDto(data = sequence, status = HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody data: LoginPostDto) : ResponseDto<UserLoginDto> {
        val user = userService.login(data.id, data.password)
        return ResponseDto(data = user, status = HttpStatus.OK)
    }

    @GetMapping
    fun getUser(@AuthenticationPrincipal user: UserDetails) : ResponseDto<UserDto> {
        val userDto = userService.getUser(user.username)
        return ResponseDto(data = userDto, status = HttpStatus.OK)
    }

    @PatchMapping
    fun updateUser(@AuthenticationPrincipal user: UserDetails, @RequestBody data: UserUpdateDto) : ResponseDto<UserDto> {
        val userDto = userService.updateUser(user.username, data)
        return ResponseDto(data = userDto, status = HttpStatus.OK)
    }

    @PostMapping
    fun deleteUser(@AuthenticationPrincipal user: UserDetails, @RequestBody password: UserPasswordDto) : ResponseDto<Nothing> {
        userService.deleteUser(user.username, password.password)
        return ResponseDto(status = HttpStatus.OK)
    }

}