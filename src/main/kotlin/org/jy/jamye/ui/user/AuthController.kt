package org.jy.jamye.ui.user

import jakarta.servlet.http.HttpServletResponse
import org.jy.jamye.application.user.dto.UserLoginDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.user.service.SocialAuthService
import org.jy.jamye.security.TokenDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth")
class AuthController(private val socialAuthService: SocialAuthService) {
    val log: Logger = LoggerFactory.getLogger(AuthController::class.java)

    @Value("\${kakao.clientId}")
    private val kakaoClientId: String? = null

    @Value("\${google.client-id}")
    private val googleClientId: String? = null

    @GetMapping("/kakao/client-id")
    fun getKakaoClientId(): ResponseDto<String> {
        log.info("[카카오 client-id 조회]")
        return ResponseDto(data = kakaoClientId)
    }

    @GetMapping("/kakao/callback")
    fun handleKakaoAccessToken(@RequestParam code: String, response: HttpServletResponse): ResponseDto<UserLoginDto> {
        log.info("[카카오 인증코드 - user 정보 조회] start")
        val kakaoLogin = socialAuthService.kakaoLogin(code = code, response = response)
        log.info("[카카오 인증코드 - user 정보 조회] end")
        return ResponseDto(data = kakaoLogin)
    }

    @PostMapping("/google")
    fun googleLogin(@RequestBody request: TokenDto.GoogleToken): ResponseDto<UserLoginDto> {
        log.info("[google 인증코드 - user 정보 조회] start")
        val googleLogin = socialAuthService.googleLogin(request.token)
        log.info("[google 인증코드 - user 정보 조회] end")
        return ResponseDto(data = googleLogin)
    }

    @GetMapping("/google/client-id")
    fun getGoogleClientId(): ResponseDto<String> {
        log.info("[google client-id 조회]")
        return ResponseDto(data = googleClientId)
    }
}