package org.jy.jamye.ui

import jakarta.validation.Valid
import org.jy.jamye.application.UserApplicationService
import org.jy.jamye.application.dto.NotifyDto
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.security.TokenDto
import org.jy.jamye.ui.post.LoginPostDto
import org.jy.jamye.ui.post.UserPasswordDto
import org.jy.jamye.ui.post.UserPostDto
import org.jy.jamye.ui.post.UserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
@Validated
class UserController(
    private val userService: UserService, private val userAppService: UserApplicationService
) {
    @PostMapping("/join")
    fun createUser(@Valid @RequestBody data: UserPostDto) : ResponseDto<Long> {
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
        userAppService.deleteUser(user.username, password.password)
        return ResponseDto(status = HttpStatus.OK)
    }

    @GetMapping("/check/id/{id}")
    fun duplicateIdCheck(@PathVariable id: String) : ResponseDto<Boolean> {
        return ResponseDto(data = userAppService.duplicateIdCheck(id), status = HttpStatus.OK)
    }
    @GetMapping("/check/email/{email}")
    fun duplicateEmailCheck(@PathVariable email: String) : ResponseDto<Boolean> {
        return ResponseDto(data = userAppService.duplicateEmailCheck(email), status = HttpStatus.OK)
    }

    @PostMapping("/password/check")
    fun passwordCheck(@AuthenticationPrincipal user: UserDetails, @RequestBody data: UserPasswordDto): ResponseDto<Nothing> {
        userService.passwordCheck(user.username, data.password)
        return ResponseDto()
    }

    @PostMapping("/refresh")
    fun getAccessToken(@RequestBody token: TokenDto): ResponseDto<TokenDto> {
        return ResponseDto<TokenDto>(data = userService.getAccessToken(token.refreshToken))
    }

    @PostMapping("/notify/{notifySeq}")
    fun viewNotify(@PathVariable notifySeq: Long) : ResponseDto<NotifyDto> {
        val notify = userService.viewNotify(notifySeq)
        return ResponseDto(data = notify)
    }

    @GetMapping("/notify")
    fun getNotifyList(@AuthenticationPrincipal user: UserDetails): ResponseDto<List<NotifyDto>> {
        val notifyList = userAppService.getNotifyList(user.username)
        return ResponseDto(data = notifyList)
    }
    @GetMapping("/no-read")
    fun getNotifyNoReadCount(@AuthenticationPrincipal user: UserDetails) : ResponseDto<Long> {
        val noReadCount = userAppService.getNotifyNoReadCount(user.username)
        return ResponseDto(data = noReadCount)
    }
}