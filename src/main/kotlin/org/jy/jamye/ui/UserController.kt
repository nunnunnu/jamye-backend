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

    @GetMapping("/{userSeq}")
    fun getUser(@PathVariable("userSeq") userSeq: Long) : ResponseDto<UserDto> {
        val user = userService.getUser(userSeq)
        return ResponseDto(data = user, status = HttpStatus.OK)
    }

    @PatchMapping("/{userSeq}")
    fun updateUser(@PathVariable("userSeq") userSeq: Long, @RequestBody data: UserUpdateDto) : ResponseDto<UserDto> {
        val user = userService.updateUser(userSeq, data)
        return ResponseDto(data = user, status = HttpStatus.OK)
    }

    @DeleteMapping("/{userSeq}")
    fun deleteUser(@PathVariable("userSeq") userSeq: Long, @RequestBody password: UserPasswordDto) : ResponseDto<Nothing> {
        userService.deleteUser(userSeq, password.password)
        return ResponseDto(status = HttpStatus.OK)
    }

}