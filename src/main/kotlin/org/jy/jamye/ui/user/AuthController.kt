package org.jy.jamye.ui.user

import jakarta.servlet.http.HttpServletResponse
import org.jy.jamye.application.user.dto.UserLoginDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.user.service.SocialAuthService
import org.jy.jamye.security.TokenDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth")
class AuthController(private val socialAuthService: SocialAuthService) {
    @Value("\${kakao.clientId}")
    private val kakaoClientId: String? = null
    @Value("\${google.client-id}")
    private val googleClientId: String? = null

    @GetMapping("/kakao/client-id")
    fun getKakaoClientId(): ResponseDto<String> {
        return ResponseDto(data = kakaoClientId)
    }
    @GetMapping("/kakao/callback")
    fun handleKakaoAccessToken(@RequestParam code: String, response: HttpServletResponse): ResponseDto<UserLoginDto> {
        val kakaoLogin = socialAuthService.kakaoLogin(code = code, response = response)
        return ResponseDto(data = kakaoLogin)
    }
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: TokenDto.GoogleToken): ResponseDto<UserLoginDto> {
        val googleLogin = socialAuthService.googleLogin(request.token)
        return ResponseDto(data = googleLogin)
    }

    @GetMapping("/google/client-id")
    fun getGoogleClientId(): ResponseDto<String> {
        return ResponseDto(data = googleClientId)
    }
}