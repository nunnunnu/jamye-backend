package org.jy.jamye.ui.user

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.jy.jamye.application.user.UserApplicationService
import org.jy.jamye.application.user.dto.NotifyDto
import org.jy.jamye.application.user.dto.UserDto
import org.jy.jamye.application.user.dto.UserLoginDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.user.model.LoginType
import org.jy.jamye.domain.user.service.UserService
import org.jy.jamye.security.TokenDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
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
    val log = LoggerFactory.getLogger(UserController::class.java)!!

    @PostMapping("/join")
    fun createUser(@Valid @RequestBody data: UserPostDto) : ResponseDto<Long> {
        log.info("[createUser] start")
        val sequence = userService.createUser(
            UserDto(id = data.id, email = data.email, password = data.password,)
        )
        log.info("[createUser] end")
        return ResponseDto(data = sequence, status = HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody data: LoginPostDto) : ResponseDto<UserLoginDto> {
        log.info("[login] start")
        LoginType.entries.forEach{ if(it.basicPassword.equals(data.password)) {
                log.info("[login] 실패 - 소셜 로그인 비밀번호 입력")
                throw throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
            }
        }
        val user = userService.login(data.id, data.password)
        log.info("[login] end")
        return ResponseDto(data = user)
    }

    @GetMapping
    fun getUser(@AuthenticationPrincipal user: UserDetails) : ResponseDto<UserDto> {
        log.info("[유저 정보 조회] start")
        val userDto = userService.getUser(user.username)
        log.info("[유저 정보 조회] end")
        return ResponseDto(data = userDto)
    }

    @PatchMapping
    fun updateUser(@AuthenticationPrincipal user: UserDetails, @RequestBody data: UserUpdateDto) : ResponseDto<UserDto> {
        log.info("[유저 정보 수정] start")
        val userDto = userService.updateUser(user.username, data)
        log.info("[유저 정보 수정] end")
        return ResponseDto(data = userDto)
    }

    @PostMapping
    fun deleteUser(@AuthenticationPrincipal user: UserDetails, @RequestBody password: UserPasswordDto) : ResponseDto<Nothing> {
        log.info("[유저 탈퇴] start")
        userAppService.deleteUser(user.username, password.password)
        log.info("[유저 탈퇴] end")
        return ResponseDto()
    }

    @GetMapping("/check/id/{id}")
    fun duplicateIdCheck(@PathVariable id: String) : ResponseDto<Boolean> {
        log.info("[중복 아이디 체크] start")
        val duplicateIdCheck = userAppService.duplicateIdCheck(id)
        log.info("[중복 아이디 체크] end")
        return ResponseDto(data = duplicateIdCheck)
    }

    @GetMapping("/check/email/{email}")
    fun duplicateEmailCheck(@PathVariable email: String): ResponseDto<Boolean> {
        log.info("[중복 이메일 체크] start")
        val duplicateEmailCheck = userAppService.duplicateEmailCheck(email)
        log.info("[중복 이메일 체크] end")
        return ResponseDto(data = duplicateEmailCheck)
    }

    @PostMapping("/password/check")
    fun passwordCheck(@AuthenticationPrincipal user: UserDetails, @RequestBody data: UserPasswordDto): ResponseDto<Nothing> {
        log.info("[비밀번호 검증] start")
        userService.passwordCheck(user.username, data.password)
        log.info("[비밀번호 검증] end")
        return ResponseDto()
    }

    @PostMapping("/refresh")
    fun getAccessToken(@RequestBody token: TokenDto): ResponseDto<TokenDto> {
        log.info("[access token 재발급] start")
        val accessToken = userService.getAccessToken(token.refreshToken)
        log.info("[access token 재발급] end")
        return ResponseDto(data = accessToken)
    }

    //TODO: notify controller 생성(응집도 향상)
    @PostMapping("/notify/{notifySeq}")
    fun viewNotify(@PathVariable notifySeq: Long) : ResponseDto<NotifyDto> {
        log.info("[알람함 읽음 처리] start")
        val notify = userService.viewNotify(notifySeq)
        userService.getNotifyNoReadCount(notify.userSeq) //todo: 쓰레드 분리 필요
        log.info("[알람함 읽음 처리] end")
        return ResponseDto(data = notify)
    }

    @GetMapping("/notify")
    fun getNotifyList(@AuthenticationPrincipal user: UserDetails): ResponseDto<List<NotifyDto>> {
        log.info("[알람함 조회] start")
        val notifyList = userAppService.getNotifyList(user.username)
        log.info("[알람함 조회] end")
        return ResponseDto(data = notifyList)
    }

    @GetMapping("/no-read")
    fun getNotifyNoReadCount(@AuthenticationPrincipal user: UserDetails) : ResponseDto<Long> {
        log.info("[안읽은 알람 갯수 조회] start")
        val noReadCount = userAppService.getNotifyNoReadCount(user.username)
        log.info("[안읽은 알람 갯수 조회] end")
        return ResponseDto(data = noReadCount)
    }

    @PostMapping("/notify/read/all")
    fun allNotifyRead(@AuthenticationPrincipal user: UserDetails) : ResponseDto<Nothing> {
        log.info("[모든 알람 읽음 처리] start")
        userAppService.allNotifyRead(user.username)
        log.info("[모든 알람 읽음 처리] end")
        return ResponseDto()
    }

    @DeleteMapping("/notify/read/delete")
    fun allReadNotifyDelete(@AuthenticationPrincipal user: UserDetails) : ResponseDto<Nothing> {
        log.info("[읽은 알람 삭제 처리] start")
        userAppService.deleteReadNotify(user.username)
        log.info("[읽은 알람 삭제 처리] end")
        return ResponseDto()
    }

    @DeleteMapping("/notify/delete/{notifySeq}")
    fun notifyDelete(@AuthenticationPrincipal user: UserDetails, @PathVariable notifySeq: Long) : ResponseDto<Nothing> {
        log.info("[알람 삭제 처리] start")
        userAppService.deleteNotify(user.username, notifySeq)
        log.info("[알람 삭제 처리] end")
        return ResponseDto()
    }

    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal user: UserDetails, request: HttpServletRequest) : ResponseDto<Nothing> {
        log.info("[로그아웃] start")
        val authHeader = request.getHeader("Authorization")
        val accessToken = authHeader.substringAfter("Bearer ")
        val refreshToken = request.getHeader("refreshToken")
        userAppService.logout(user.username, accessToken, refreshToken!!)
        log.info("[로그아웃] end")
        return ResponseDto()
    }
}